# Globs Command Line

Turn command-line arguments — or environment variables — into a [Glob](https://globsframework.org). The
`GlobType` is the whole declaration of the options: one field per option, its Java type gives the
conversion, and a few annotations cover defaults, mandatory options, positional arguments and sub-commands.
No reflection, no annotation processor, no option object to keep in sync.

## Requirements

Java 21 and `org.globsframework:globs`.

## Installation

```xml
<dependency>
    <groupId>org.globsframework</groupId>
    <artifactId>globs-commandline</artifactId>
    <version>5.0.0</version>
</dependency>
```

## Parsing a command line

For the arguments:

```
--value toto titi --otherName tata --value A,B,C --name "a name"
```

with the type:

```java
public static class Opt1 {
    public static final GlobType TYPE;

    public static final StringField NAME;
    public static final StringArrayField MULTIVALUES;
    public static final IntegerField VAL;

    static {
        GlobTypeBuilder typeBuilder = GlobTypeBuilderFactory.create("opt1");
        NAME = typeBuilder.declareStringField("name");
        MULTIVALUES = typeBuilder.declareStringArrayField("value", ArraySeparator.create(','));
        VAL = typeBuilder.declareIntegerField("val", DefaultInteger.create(123));
        TYPE = typeBuilder.build();
    }
}
```

parsed with:

```java
Glob opt = ParseCommandLine.parse(Opt1.TYPE, args, true);   // true: ignore unknown parameters
```

you get:

```java
Assert.assertEquals("a name", opt.get(Opt1.NAME));
Assert.assertArrayEquals(new String[]{"toto", "titi", "A", "B", "C"}, opt.get(Opt1.MULTIVALUES));
Assert.assertEquals(123, opt.get(Opt1.VAL).intValue());
```

Rules worth knowing:

- An option is `--<field name>`, or `--<name>` from `FieldName.create(...)`.
- A `BooleanField` is a flag: `--verbose` alone sets it to true.
- An **array field keeps consuming** the following bare arguments, and each of them is also split on the
  separator — `,` by default, changed with `ArraySeparator`. That is why `--value toto titi` and
  `--value A,B,C` both feed `MULTIVALUES`.
- Fields with a `Default*` annotation are filled in before parsing, so an absent option keeps its default.
- A field annotated `Mandatory` that ends up unset raises a `ParseError`, as does an unknown parameter
  unless `ignoreUnknown` is true. With `ignoreUnknown`, the arguments that were not consumed are written
  back into the `List<String>` passed in, so a second pass can take them.

### Positional arguments

A field annotated `UnNamed` takes the bare arguments in declaration order; a trailing `StringArrayField`
takes the rest:

```java
firstArg  = typeBuilder.declareStringField("firstArg", UnNamed.UNIQUE);
otherArgs = typeBuilder.declareStringArrayField("otherArgs", UnNamed.UNIQUE);

Glob option = ParseCommandLine.parse(Arg.TYPE, new String[]{"A1", "A2", "A3"});
// firstArg = "A1", otherArgs = ["A2", "A3"]
```

### Sub-commands

A `GlobUnionField` makes a bare argument select one of its target types by name, and the options that follow
are parsed into that sub-Glob — `git commit --amend` shaped command lines:

```java
name = builder.declareStringField("name");
cmd  = builder.declareGlobUnionField("cmd", new Supplier[]{() -> Cmd1.TYPE, () -> Cmd2.TYPE});

Glob options = ParseCommandLine.parse(Options.TYPE,
        List.of("--name", "ZZZ", "cmd1", "--arg1", "v1", "--arg2", "v2"), true);
options.get(Options.cmd).getType();          // Cmd1.TYPE
options.get(Options.cmd).get(Cmd1.arg1);     // "v1"
```

The sub-command may come before or after the global options.

### The other direction

`ParseCommandLine.toArgs(glob)` renders a Glob back into a `String[]`, which is how a process spawns another
with the options it was given (String, Integer, Double, Boolean and StringArray fields; anything else
throws).

## Reading environment variables

The same type also describes the environment. The variable name is
`<PREFIX>_<TYPE NAME>_<FIELD NAME>`, upper-cased, with camelCase turned into `SNAKE_CASE` and `.` replaced
by `_`:

```java
// PREFIX_OPT1_NAME=a name
Glob opt = ParseEnvironment.parseEnv("PREFIX", Opt1.TYPE);
```

Defaults and `Mandatory` behave as they do on the command line, a missing mandatory variable raising
`EnvironmentVariableNotSetException`. Both entry points also accept an already-filled `Glob` instead of a
`GlobType`, so the two sources compose: parse the environment first, then let the command line override it.

## Building

```bash
mvn -o test          # JUnit 4
```

## License

Apache License 2.0 — see <https://www.apache.org/licenses/LICENSE-2.0.txt>.

## Links

- [Globs Framework](https://globsframework.org)
- [GitHub repository](https://github.com/globsframework/globs-commandline)
