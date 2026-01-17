package org.globsframework.commandline;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.annotations.DefaultString;
import org.globsframework.core.metamodel.annotations.DefaultString_;
import org.globsframework.core.metamodel.annotations.FieldName_;
import org.globsframework.core.metamodel.fields.IntegerField;
import org.globsframework.core.metamodel.fields.StringArrayField;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.Glob;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ParseEnvironmentTest {

    @Test
    public void name() throws Exception {
        Map<String, String> env = new HashMap<>();
        env.put("PREFIX_OPT1_NAME", "a name");
        Glob opt = ParseEnvironment.parse("PREFIX", Opt1.TYPE, env);
        Assert.assertEquals("a name", opt.get(Opt1.NAME));
    }

    @Test
    public void multipleOptions() throws Exception {
        Map<String, String> env = new HashMap<>();
        env.put("PREFIX_OPTMULTI_NAME", "a name");
        env.put("PREFIX_OPTMULTI_ANOTHER_NAME", "another name");

        Glob opt = ParseEnvironment.parse("PREFIX", OptMulti.TYPE, env);
        Assert.assertEquals("a name", opt.get(OptMulti.NAME));
        Assert.assertEquals("another name", opt.get(OptMulti.ANOTHER_NAME));
    }

    @Test
    public void testForDefaultValues() throws Exception {
        Map<String, String> env = new HashMap<>();
        env.put("PREFIX_OPTDEFAULT_NAME", "a name");

        Glob opt = ParseEnvironment.parse("PREFIX", OptDefault.TYPE, env);
        Assert.assertEquals("a name", opt.get(OptDefault.NAME));
        Assert.assertEquals("Marc", opt.get(OptDefault.ANOTHER_NAME));
    }

    @Test
    public void testWithIntValues() throws Exception {
        Map<String, String> env = new HashMap<>();
        env.put("PREFIX_OPTWITHINTS_NAME", "a name");
        env.put("PREFIX_OPTWITHINTS_INT_FIELD", "42");

        Glob opt = ParseEnvironment.parse("PREFIX", OptWithInts.TYPE, env);
        Assert.assertEquals(42, opt.get(OptWithInts.INT_FIELD).intValue());
    }

    @Test(expected = EnvironmentVariableNotSetException.class)
    public void multipleOptionsExceptionTest() throws Exception {
        Map<String, String> env = new HashMap<>();
        env.put("PREFIX_OPTMULTI_ANOTHER_NAME", "another name");

        Glob opt = ParseEnvironment.parse("PREFIX", OptMulti.TYPE, env);

    }

    @Test
    public void arrayFieldTest() throws Exception {

        Map<String, String> env = new HashMap<>();
        env.put("PREFIX_OPTWITHSTRINGARRAY_ARRAY_FIELD", "another,name");

        Glob opt = ParseEnvironment.parse("PREFIX", OptWithStringArray.TYPE, env);

        Assert.assertEquals(2, opt.get(OptWithStringArray.arrayField).length);
        Assert.assertEquals("another", opt.get(OptWithStringArray.arrayField)[0]);
        Assert.assertEquals("name", opt.get(OptWithStringArray.arrayField)[1]);

    }

    @Test
    public void fieldWithPoint() throws Exception {
        Map<String, String> env = new HashMap<>();
        env.put("PREFIX_OPTWITHPOINT_POINT_FIELD", "test");

        {
            Glob opt = ParseEnvironment.parse("PREFIX", OptWithPoint.TYPE, env);

            Assert.assertEquals("test", opt.get(OptWithPoint.withPoint));
        }

        {
            Glob opt = ParseCommandLine.parse(OptWithPoint.TYPE, new String[]{"--point.field", "test"});
            Assert.assertEquals("test", opt.get(OptWithPoint.withPoint));
        }

    }


    public static class Opt1 {
        public static GlobType TYPE;

        public static StringField NAME;

        static {
            GlobTypeBuilder typeBuilder = GlobTypeBuilderFactory.create("opt1");
            NAME = typeBuilder.declareStringField("name");
            TYPE = typeBuilder.build();
        }
    }

    public static class OptMulti {
        public static GlobType TYPE;

        @Mandatory_
        public static StringField NAME;
        public static StringField ANOTHER_NAME;

        static {
            GlobTypeBuilder typeBuilder = GlobTypeBuilderFactory.create("optMulti");
            NAME = typeBuilder.declareStringField("name", Mandatory.UNIQUE);
            ANOTHER_NAME = typeBuilder.declareStringField("anotherName");
            TYPE = typeBuilder.build();
        }
    }

    public static class OptDefault {
        public static GlobType TYPE;

        public static StringField NAME;
        @DefaultString_("Marc")
        public static StringField ANOTHER_NAME;

        static {
            GlobTypeBuilder  typeBuilder = GlobTypeBuilderFactory.create("optDefault");
            NAME = typeBuilder.declareStringField("name");
            ANOTHER_NAME = typeBuilder.declareStringField("anotherName", DefaultString.create("Marc"));
            TYPE = typeBuilder.build();
        }
    }

    public static class OptWithInts {
        public static GlobType TYPE;

        public static StringField NAME;
        public static IntegerField INT_FIELD;

        static {
            GlobTypeBuilder typeBuilder = GlobTypeBuilderFactory.create("optWithInts");
            NAME = typeBuilder.declareStringField("name");
            INT_FIELD = typeBuilder.declareIntegerField("intField");
            TYPE = typeBuilder.build();
        }
    }

    public static class OptWithStringArray {
        public static GlobType TYPE;

        public static StringField NAME;
        public static StringArrayField arrayField;

        static {
            GlobTypeBuilder typeBuilder = GlobTypeBuilderFactory.create("optWithStringArray");
            NAME = typeBuilder.declareStringField("name");
            arrayField = typeBuilder.declareStringArrayField("arrayField");
            TYPE = typeBuilder.build();
        }
    }

    public static class OptWithPoint {
        public static GlobType TYPE;

        public static StringField NAME;

        @FieldName_("point.field")
        public static StringField withPoint;

        static {
            GlobTypeBuilder typeBuilder = GlobTypeBuilderFactory.create("optWithPoint");
            NAME = typeBuilder.declareStringField("name");
            withPoint = typeBuilder.declareStringField("point.field");
            TYPE = typeBuilder.build();
        }
    }

}
