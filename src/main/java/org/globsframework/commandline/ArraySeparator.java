package org.globsframework.commandline;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.annotations.GlobCreateFromAnnotation;
import org.globsframework.core.metamodel.annotations.InitUniqueKey;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.metamodel.impl.DefaultGlobTypeBuilder;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.Key;
import org.globsframework.core.model.KeyBuilder;

public class ArraySeparator {
    public static final GlobType TYPE;

    public static final StringField SEPARATOR;

    @InitUniqueKey
    public static final Key KEY;

    public static Glob create(char separator) {
        return TYPE.instantiate()
                .set(SEPARATOR, String.valueOf(separator));
    }

    static {
        GlobTypeBuilder typeBuilder = new DefaultGlobTypeBuilder("ArraySeparator");
        SEPARATOR = typeBuilder.declareStringField("separator");
        typeBuilder.register(GlobCreateFromAnnotation.class, annotation -> ArraySeparator.TYPE.instantiate()
                .set(SEPARATOR, String.valueOf(((ArraySeparator_) annotation).value())));
        TYPE = typeBuilder.build();
        KEY = KeyBuilder.newEmptyKey(TYPE);
    }
}
