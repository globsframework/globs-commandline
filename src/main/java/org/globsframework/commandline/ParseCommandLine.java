package org.globsframework.commandline;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.Glob;
import org.globsframework.model.MutableGlob;
import org.globsframework.utils.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ParseCommandLine {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParseCommandLine.class);

    private ParseCommandLine() {
    }

    public static String[] toArgs(Glob glob) {
        List<String> args = new ArrayList<>();
        for (Field field : glob.getType().getFields()) {
            if (!glob.isNull(field)) {
                if (field instanceof StringField) {
                    args.add("--" + field.getName());
                    args.add(glob.get((StringField) field));
                } else if (field instanceof DoubleField) {
                    args.add("--" + field.getName());
                    args.add(glob.get((DoubleField) field).toString());
                } else if (field instanceof IntegerField) {
                    args.add("--" + field.getName());
                    args.add(glob.get((IntegerField) field).toString());
                } else if (field instanceof StringArrayField) {
                    String[] values = glob.getOrEmpty((StringArrayField) field);
                    args.add("--" + field.getName());
                    args.addAll(Arrays.asList(values));
                } else if (field instanceof BooleanField) {
                    args.add("--" + field.getName());
                } else {
                    throw new RuntimeException("for " + field.getDataType() + " not managed for " + field.getName());
                }
            }
        }
        return args.toArray(new String[0]);
    }

    public static Glob parse(GlobType type, String[] line) {
        return parse(type, new ArrayList<>(Arrays.asList(line)), false);
    }

    public static Glob parse(GlobType type, List<String> line, boolean ignoreUnknown) {
        return parse(type, line, ignoreUnknown, false);
    }

    public static Glob parse(GlobType type, List<String> line, boolean ignoreUnknown, boolean stopAtFirstNotFound) {
        LOGGER.info("parse: " + line);
        ArrayDeque<String> deque = new ArrayDeque<>(line);
        ArrayDeque<String> ignored = new ArrayDeque<>();
        MutableGlob glob = extract(type, ignoreUnknown, stopAtFirstNotFound, deque, ignored);
        line.clear();
        line.addAll(ignored);
        line.addAll(deque);
        return glob;
    }

    private static MutableGlob extract(GlobType type, boolean ignoreUnknown, boolean stopAtFirstNotFound, Deque<String> deque, Deque<String> ignored) {
        MutableGlob instantiate = type.instantiate();
        Field[] fields = type.getFields();

        Deque<Field> withoutSpecifier = Arrays.stream(fields).filter(field -> field.hasAnnotation(UnNamed.KEY))
                .collect(Collectors.toCollection(ArrayDeque::new));

        setDefaultValues(fields, instantiate);

        if (!deque.isEmpty()) {
            int size;
            Field lastField = null;
            do {
                size = deque.size();
                String param = deque.peekFirst();
                if (param.startsWith("--")) {
                    String name = param.substring(2);
                    lastField = type.findField(name);
                    if (lastField != null) {
                        deque.removeFirst();
                        if (ParseUtils.fieldIsABoolean(lastField)) {
                            instantiate.setValue(lastField, Boolean.TRUE);
                        } else {
                            assignValueToField(lastField, deque, instantiate, param);
                        }
                    } else if (stopAtFirstNotFound) {
                        return instantiate;
                    } else if (!ignoreUnknown) {
                        throw new ParseError("Unknown parameter " + name);
                    }
                    else {
                        ignored.addLast(deque.pollFirst());
                    }
                } else if (lastField != null && lastField.getDataType().isArray()) {
                    StringConverter.FromStringConverter converter = StringConverter.createConverter(lastField,
                            lastField.findOptAnnotation(ArraySeparator.KEY).map(glob -> glob.get(ArraySeparator.SEPARATOR)).orElse(","));
                    converter.convert(instantiate, param);
                    deque.removeFirst();
                } else {
                    if (lastField == null) {
                        if (!withoutSpecifier.isEmpty()) {
                            Field field = withoutSpecifier.removeFirst();
                            assignValueToField(field, deque, instantiate, param);
                            if (field instanceof StringArrayField) {
                                lastField = field;
                            }
                            continue;
                        }
                    }

                    Optional<GlobUnionField> optionalUnion = type.streamFields().filter(field -> field instanceof GlobUnionField)
                            .map(field -> ((GlobUnionField) field))
                            .findFirst();
                    Optional<GlobType> tType = optionalUnion
                            .stream()
                            .flatMap(field -> field.getTargetTypes().stream())
                            .filter(globType -> param.equals(globType.getName()))
                            .peek(globType -> deque.removeFirst())
                            .peek(globType -> instantiate.set(optionalUnion.get(),
                                    extract(globType, true, true, deque, ignored)))
                            .findFirst();

                    if (optionalUnion.isEmpty() || tType.isEmpty()) {
                        if (stopAtFirstNotFound) {
                            break;
                        }
                        if (!ignoreUnknown) {
                            throw new ParseError("Unknown parameter '" + param + "'");
                        }
                        else {
                            ignored.addLast(deque.pollFirst());
                        }
                    } else {
                        lastField = null;
                    }
                }
            } while (deque.size() != 0 && size != deque.size());
        }

        checkMandatoryFields(type, instantiate);
        LOGGER.info("Return : " + instantiate.toString());
        return instantiate;
    }

    private static void checkMandatoryFields(GlobType type, MutableGlob instantiate) {
        for (Field field : type.getFields()) {
            if (!instantiate.isSet(field) && field.hasAnnotation(Mandatory.KEY)) {
                throw new ParseError("Missing argument " + field);
            }
        }
    }

    private static void assignValueToField(Field lastField, Deque<String> deque, MutableGlob instantiate, String s) {
        if (deque.isEmpty()) {
            throw new ParseError("Missing parameter for " + s);
        }
        StringConverter.FromStringConverter converter = StringConverter.createConverter(lastField,
                lastField.findOptAnnotation(ArraySeparator.KEY).map(ArraySeparator.SEPARATOR).orElse(","));
        converter.convert(instantiate, deque.pollFirst());
    }

    private static void setDefaultValues(Field[] fields, MutableGlob instantiate) {

        for (Field field : fields) {
            if (ParseUtils.fieldHasDefaultValue(field)) {
                instantiate.setValue(field, field.getDefaultValue());
            }
        }
    }
}
