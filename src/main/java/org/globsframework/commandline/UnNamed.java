package org.globsframework.commandline;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.impl.DefaultGlobTypeBuilder;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.Key;
import org.globsframework.core.model.KeyBuilder;

public class UnNamed {
    public static final GlobType TYPE;

    public static final Key KEY;

    public static final Glob UNIQUE;

    static {
        GlobTypeBuilder typeBuilder = new DefaultGlobTypeBuilder("UnNamed");
        TYPE = typeBuilder.build();
        KEY = KeyBuilder.newEmptyKey(TYPE);
        UNIQUE = TYPE.instantiate();
    }

    private static Glob getUnique() {
        return UNIQUE;
    }
}
