package org.globsframework.commandline;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.annotations.GlobCreateFromAnnotation;
import org.globsframework.core.metamodel.annotations.InitUniqueGlob;
import org.globsframework.core.metamodel.annotations.InitUniqueKey;
import org.globsframework.core.metamodel.impl.DefaultGlobTypeBuilder;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.Key;
import org.globsframework.core.model.KeyBuilder;

public class UnNamed {
    public static final GlobType TYPE;

    @InitUniqueKey
    public static final Key KEY;

    @InitUniqueGlob
    public static final Glob UNIQUE;

    static {
        GlobTypeBuilder typeBuilder = new DefaultGlobTypeBuilder("UnNamed");
        typeBuilder.register(GlobCreateFromAnnotation.class, annotation -> getUnique());
        TYPE = typeBuilder.build();
        KEY = KeyBuilder.newEmptyKey(TYPE);
        UNIQUE = TYPE.instantiate();
    }

    private static Glob getUnique() {
        return UNIQUE;
    }
}
