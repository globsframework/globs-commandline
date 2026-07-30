# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

The workspace-level `/home/guiot/dev/globs/CLAUDE.md` describes the globs ecosystem and conventions shared by
all the sibling repos; this file only covers what is specific to `globs-commandline`.

## What this module is

`argv` (or environment variables) → `Glob`. The caller declares a `GlobType` whose fields *are* the options,
and gets back a `Glob` instance. Two entry points, one shared annotation set:

- `ParseCommandLine.parse(GlobType|Glob, String[]|List<String>, ignoreUnknown?, stopAtFirstNotFound?)`
- `ParseEnvironment.parseEnv(prefix, GlobType|Glob)` — package-visible `parse(prefix, type, Map)` overload is
  what the tests use to inject a fake environment.
- `ParseCommandLine.toArgs(Glob)` — the reverse direction, but *not* a full inverse (see below).

The whole module is ~400 lines in `org.globsframework.commandline`; there is no framework, just two parsers
plus three annotations (`Mandatory`, `UnNamed`, `ArraySeparator`) and `ParseError` / `EnvironmentVariableNotSetException`.

## Build

Java 21 source/target, local JDK 25, JUnit 4. Depends on `org.globsframework:globs` **5.3.0** from `~/.m2`
(module's own version is 5.1-SNAPSHOT — the numbers are unrelated).

```bash
mvn -o test                              # offline works, ~/.m2 is warm
mvn -o test -Dtest=ParseCommandLineTest#WithMultiValueInArray
mvn -o install                           # publish to ~/.m2 for dependent repos
```

CI (`.github/workflows/`) runs `mvn -s settings.xml -B package` on **JDK 17** even though the pom targets 21.

## Architecture notes that are not obvious from one file

**String → field conversion is not implemented here.** Both parsers delegate to
`StringConverter.createConverter(field, separator)` from globs core, which owns the per-`DataType` logic and
writes straight into the `MutableGlob`. Consequences: supporting a new option type is a change in *core*, not
in this repo, and the `@Ignore`d `ParseCommandLineTest#testWithDate` documents that date/datetime fields are
not (yet) handled by that converter.

**`parse(…, List<String> line, …)` mutates its argument.** On return, `line` has been cleared and refilled
with the tokens that were ignored plus whatever was left unconsumed. That is the intended way to parse one
argv against several unrelated `GlobType`s: parse type A with `ignoreUnknown=true`, then hand the same list
to type B (`ParseCommandLineTest#multipleOptions`). The `String[]` overloads copy first, so they don't.

**`extract()` is a `lastField`-stateful loop** (`ParseCommandLine.java:82`):

- `--foo` looks up the field by *field name* (`type.findField`), so `@FieldName_("point.field")` makes
  `--point.field` valid; a Boolean field consumes no value (presence ⇒ `TRUE`), anything else consumes the
  next token.
- A bare token while `lastField` is an array field is appended to that array — this is how
  `--value toto titi --value A,B,C` accumulates five entries. Each token is additionally split on the
  separator from `@ArraySeparator_(',')` (default `,`).
- A bare token with no `lastField` is fed to the next field annotated `@UnNamed_`, in declaration order;
  a `StringArrayField` in that position becomes `lastField` and swallows the rest.
- A bare token matching the *name of a target type* of a `GlobUnionField` is a **subcommand**: `extract()`
  recurses into that type with `ignoreUnknown=true, stopAtFirstNotFound=true`, so the sub-parse hands
  unrecognised options back to the parent (`MultiLevelCommandLineTest`).
- The loop guard is `while (!deque.isEmpty() && size != deque.size())` — an iteration that consumes nothing
  ends parsing silently. Any new branch **must** remove from the deque or the change will look like a no-op.

Defaults are applied before parsing (`field.getDefaultValue()`, i.e. `@DefaultInteger_` & co from core) and
`@Mandatory_` is checked at the end against `isSet`, not `isNull`.

**`toArgs` is deliberately narrower than `parse`**: it only emits String/Double/Integer/StringArray/Boolean
and throws on anything else; array values are emitted as bare tokens after the `--name`, and a false Boolean
is omitted. Round-tripping only holds for types built from those five kinds.

**Env var naming**: `PREFIX_TYPENAME_FIELD_IN_SNAKE_CASE`, uppercased, with `.` replaced by `_` — so the
field `point.field` on type `optWithPoint` reads `PREFIX_OPTWITHPOINT_POINT_FIELD`. The type name is part of
the variable, which is what lets one prefix serve several option types.

## Conventions in this module

Annotations follow the ecosystem's `Foo` / `Foo_` pair, built with `new DefaultGlobTypeBuilder(...)` +
`register(GlobCreateFromAnnotation.class, …)`. There is **no** `AllXAnnotations` registry type here, unlike
most sibling repos — adding an annotation means the two files only.

Tests (JUnit 4) declare their option types as static nested classes whose `GlobType` is built in a static
block with `GlobTypeBuilderFactory.create(name)`, passing annotations as Globs to `declareXField`
(`declareStringField("name", Mandatory.UNIQUE)`) while also carrying the java `@Mandatory_` for readability.
Follow that shape rather than introducing a `Dummy*` type.
