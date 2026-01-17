package org.globsframework.commandline;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.annotations.DefaultInteger;
import org.globsframework.core.metamodel.annotations.DefaultInteger_;
import org.globsframework.core.metamodel.annotations.FieldName_;
import org.globsframework.core.metamodel.fields.*;
import org.globsframework.core.model.Glob;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

public class ParseCommandLineTest {

    @Test
    public void name() {
        Glob opt = ParseCommandLine.parse(Opt1.TYPE, new String[]{"--name", "a name"});
        Assert.assertEquals("a name", opt.get(Opt1.NAME));
        Assert.assertEquals(123, opt.get(Opt1.VAL).intValue());

        String[] args = {"--name", "a name", "--val", "321"};
        opt = ParseCommandLine.parse(Opt1.TYPE, args);
        Assert.assertEquals("a name", opt.get(Opt1.NAME));
        Assert.assertEquals(321, opt.get(Opt1.VAL).intValue());
        Assert.assertArrayEquals(args, ParseCommandLine.toArgs(opt));

        opt = ParseCommandLine.parse(Opt1.TYPE, new String[]{"--value", "1", "--value", "2"});
        String[] strings = opt.getOrEmpty(Opt1.MULTIVALUES);
        Assert.assertEquals(2, strings.length);
        Assert.assertEquals("1,2", String.join(",", strings));
    }

    @Test
    public void checkMandatoryAreChecked() {
        ArrayList<String> args = new ArrayList<>();
        try {
            Glob opt = ParseCommandLine.parse(OptWithMandatory.TYPE, args, true);
            Assert.fail("mandatory not checked");
        } catch (ParseError parseError) {
        }
    }

    @Test
    public void multipleOptions() {
        ArrayList<String> args = new ArrayList<>(Arrays.asList("--name", "a name", "--otherName", "titi"));
        Glob opt = ParseCommandLine.parse(Opt1.TYPE, args, true);
        Assert.assertEquals("a name", opt.get(Opt1.NAME));
        Glob opt2 = ParseCommandLine.parse(Opt2.TYPE, args, false);
        Assert.assertEquals("titi", opt2.get(Opt2.otherName));
    }

    @Test
    public void withUnknownParam() {
        ArrayList<String> args = new ArrayList<>(Arrays.asList("--name", "a name", "--otherName", "titi"));
        try {
            Glob opt = ParseCommandLine.parse(Opt1.TYPE, args, false);
            Assert.fail("not ignored");
        } catch (Exception exception) {
            Assert.assertTrue(exception instanceof ParseError);
        }
    }

    @Test
    public void WithMultiValueInArray() {
        ArrayList<String> args = new ArrayList<>(Arrays.asList("--value", "toto:ddd", "titi", "--value", "A,B,C", "--name", "a name"));
        Glob opt = ParseCommandLine.parse(Opt1.TYPE, args, false);
        Assert.assertEquals("a name", opt.get(Opt1.NAME));
        Assert.assertArrayEquals(new String[]{"toto:ddd", "titi", "A", "B", "C"}, opt.get(Opt1.MULTIVALUES));
    }

    @Test
    public void WithMultiValueInArrayWithMultiOpt() {
        ArrayList<String> args = new ArrayList<>(Arrays.asList("--value", "toto", "titi", "--otherName", "tata", "--value", "A,B,C", "--name", "a name"));
        Glob opt = ParseCommandLine.parse(Opt1.TYPE, args, true);
        Assert.assertEquals("a name", opt.get(Opt1.NAME));
        Assert.assertArrayEquals(new String[]{"toto", "titi", "A", "B", "C"}, opt.get(Opt1.MULTIVALUES));
        Glob opt2 = ParseCommandLine.parse(Opt2.TYPE, args, true);
        Assert.assertEquals("tata", opt2.get(Opt2.otherName));
    }


    @Test
    @Ignore
    public void testWithDate() {
        Glob opt1 = ParseCommandLine.parse(ArgWithDate.TYPE, new String[]{"--dateTime", "2021-02-10T00:00:00+00"});
        Assert.assertEquals(ZonedDateTime.parse("2021-02-10T00:00:00+00"), opt1.get(ArgWithDate.dateTime));

        Glob opt2 = ParseCommandLine.parse(ArgWithDate.TYPE, new String[]{"--dateTime", "2021-02-10"});
        Assert.assertEquals(ZonedDateTime.parse("2021-02-10", DateTimeFormatter.ISO_LOCAL_DATE_TIME), opt2.get(ArgWithDate.dateTime));

    }

    public static class Opt1 {
        public static GlobType TYPE;

        public static StringField NAME;

        @FieldName_("value")
        @ArraySeparator_(',')
        public static StringArrayField MULTIVALUES;

        @DefaultInteger_(123)
        public static IntegerField VAL;

        static {
            GlobTypeBuilder typeBuilder = GlobTypeBuilderFactory.create("opt1");
            NAME = typeBuilder.declareStringField("name");
            MULTIVALUES = typeBuilder.declareStringArrayField("value", ArraySeparator.create(','));
            VAL = typeBuilder.declareIntegerField("val", DefaultInteger.create(123));
            TYPE = typeBuilder.build();
        }
    }

    public static class Opt2 {
        public static final GlobType TYPE;

        public static final StringField otherName;

        static {
            GlobTypeBuilder typeBuilder = GlobTypeBuilderFactory.create("opt2");
            otherName = typeBuilder.declareStringField("otherName");
            TYPE = typeBuilder.build();
        }
    }

    public static class OptWithMandatory {
        public static GlobType TYPE;

        @Mandatory_
        public static StringField otherName;

        static {
            GlobTypeBuilder typeBuilder = GlobTypeBuilderFactory.create("optWithMandatory");
            otherName = typeBuilder.declareStringField("otherName", Mandatory.UNIQUE);
            TYPE = typeBuilder.build();
        }
    }

    public static class ArgWithDate {
        public static GlobType TYPE;

        public static DateTimeField dateTime;

        public static DateField date;

        static {
            GlobTypeBuilder typeBuilder = GlobTypeBuilderFactory.create("date");
            dateTime = typeBuilder.declareDateTimeField("dateTime");
            date = typeBuilder.declareDateField("date");
            TYPE = typeBuilder.build();
        }
    }

}
