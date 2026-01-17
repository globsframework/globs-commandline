package org.globsframework.commandline;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.annotations.Targets;
import org.globsframework.core.metamodel.fields.GlobUnionField;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.Glob;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Supplier;

public class MultiLevelCommandLineTest {


    @Test
    public void multiLevelTest() {
        Glob options1 = ParseCommandLine.parse(Options.TYPE, new ArrayList<>(Arrays.asList("cmd2", "--arg1", "v1", "--arg2", "v2", "--name", "ZZZ")), true);
        Assert.assertEquals(Cmd2.TYPE, options1.get(Options.cmd).getType());
        Assert.assertEquals("ZZZ", options1.get(Options.name));
        Glob options2 = ParseCommandLine.parse(Options.TYPE, new ArrayList<>(Arrays.asList("--name", "ZZZ", "cmd1", "--arg1", "v1", "--arg2", "v2")), true);
        Assert.assertEquals(Cmd1.TYPE, options2.get(Options.cmd).getType());
        Assert.assertEquals("v1", options2.get(Options.cmd).get(Cmd1.arg1));
        Assert.assertEquals("ZZZ", options2.get(Options.name));
    }

    public static class Options {
        public static GlobType TYPE;

        public static StringField name;

        @Targets({Cmd1.class, Cmd2.class})
        public static GlobUnionField cmd;

        static {
            GlobTypeBuilder builder = GlobTypeBuilderFactory.create("Options");
            name = builder.declareStringField("name");
            cmd = builder.declareGlobUnionField("cmd", new Supplier[]{() -> Cmd1.TYPE, () -> Cmd2.TYPE});
            TYPE = builder.build();
        }
    }

    public static class Cmd1 {
        public static GlobType TYPE;

        public static StringField arg1;
        public static StringField arg2;

        static {
            GlobTypeBuilder builder = GlobTypeBuilderFactory.create("cmd1");
            arg1 = builder.declareStringField("arg1");
            arg2 = builder.declareStringField("arg2");
            TYPE = builder.build();
        }
    }

    public static class Cmd2 {
        public static GlobType TYPE;
        public static StringField arg1;
        public static StringField arg2;

        static {
            GlobTypeBuilder builder = GlobTypeBuilderFactory.create("cmd2");
            arg1 = builder.declareStringField("arg1");
            arg2 = builder.declareStringField("arg2");
            TYPE = builder.build();
        }
    }
}
