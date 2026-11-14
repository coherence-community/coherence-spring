/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
import consumer.ReleaseContract;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import javax.tools.ToolProvider;

/** Run by the CI script regressions with a real JDK, compiler, classes and JARs. */
class ReleaseContractTests {
    interface Checked { void run() throws Exception; }

    public static void main(String[] args) throws Exception {
        Path work = Path.of(args[1]);
        switch (args[0]) {
            case "declarations" -> declarations();
            case "bytecode" -> bytecode(work);
            case "guard" -> guard(work);
            default -> throw new AssertionError(args[0]);
        }
        System.out.println(args[0] + " PASS on Java " + Runtime.version());
    }

    static void fails(String message, Checked action) throws Exception {
        try { action.run(); }
        catch (AssertionError expected) {
            if (expected.getMessage().contains(message)) return;
            throw expected;
        }
        throw new AssertionError("Expected rejection: " + message);
    }

    static void declarations() throws Exception {
        if (ReleaseContract.selected("17", "17", "17").major() != 61) throw new AssertionError("release17");
        if (Runtime.version().feature() >= 21) {
            if (ReleaseContract.selected("21", "21", "21").major() != 65) throw new AssertionError("release21");
        }
        else fails("cannot run consumer release 21", () -> ReleaseContract.selected("21", "21", "21"));
        for (String invalid : Arrays.asList(null, "", "1.8", "17.0", "017", " 17", "22", "garbage", "@consumer.release@"))
            fails("Invalid consumer.release", () -> ReleaseContract.selected(invalid, invalid, invalid));
        for (String other : Arrays.asList(null, "", "21", "@consumer.release@")) {
            fails("consumer.compiler.release", () -> ReleaseContract.selected("17", other, "17"));
            fails("Outer consumer.release", () -> ReleaseContract.selected("17", "17", other));
        }
    }

    static void bytecode(Path work) throws Exception {
        Path source = work.resolve("Example.java");
        Files.writeString(source, "public class Example { public String value() { return \"packaged\"; } }");
        for (int release : List.of(17, 21)) {
            // Java17 still exercises the release21 runtime rejection above; Java21
            // additionally compiles and checks both actual compiler targets here.
            if (Runtime.version().feature() < release) continue;
            Path classes = Files.createDirectory(work.resolve("classes" + release));
            if (ToolProvider.getSystemJavaCompiler().run(null, null, null, "--release", "" + release,
                    "-d", classes.toString(), source.toString()) != 0) throw new AssertionError("javac failed");
            var target = ReleaseContract.selected("" + release, "" + release, "" + release);
            Path jar = work.resolve("product" + release + ".jar");
            byte[] bytes = Files.readAllBytes(classes.resolve("Example.class"));
            archive(jar, bytes);
            if (ReleaseContract.directory(classes, target) != 1) throw new AssertionError("class count");
            ReleaseContract.jar(jar, target);
            var wrong = new ReleaseContract.Target(release == 17 ? 21 : 17);
            fails("actual major " + target.major(), () -> ReleaseContract.directory(classes, wrong));
            fails("actual major " + target.major(), () -> ReleaseContract.jar(jar, wrong));
            // A mixed archive/directory must fail even when the first class matches.
            bytes[7] = (byte) wrong.major();
            Files.write(classes.resolve("Wrong.class"), bytes);
            fails("actual major " + wrong.major(), () -> ReleaseContract.directory(classes, target));
            archive(jar, bytes);
            fails("actual major " + wrong.major(), () -> ReleaseContract.jar(jar, target));
            archive(jar, new byte[7]);
            fails("Invalid class file", () -> ReleaseContract.jar(jar, target));
        }
        Path empty = Files.createDirectory(work.resolve("empty"));
        fails("No compiled consumer classes", () -> ReleaseContract.directory(empty, new ReleaseContract.Target(17)));
    }

    static void archive(Path path, byte[] bytes) throws Exception {
        try (var out = new JarOutputStream(Files.newOutputStream(path))) {
            out.putNextEntry(new JarEntry("Example.class")); out.write(bytes); out.closeEntry();
        }
    }

    static void guard(Path work) throws Exception {
        ConsumerHarness.module = work;
        ConsumerHarness.target = work.resolve("target");
        ConsumerHarness.inventory = new Properties(); ConsumerHarness.inventory.setProperty("fixture", "scenario");
        Path target = ConsumerHarness.target;
        Path build = target.resolve("it/fixture/target");
        write(target.resolve("invoker-reports/BUILD-fixture.xml"), "<build-job result=\"success\"/>");
        write(build.resolve("failsafe-reports/TEST-consumer.ConsumerIT.xml"),
                "<testsuite><testcase classname=\"consumer.ConsumerIT\" name=\"scenario\"/></testsuite>");
        for (String file : List.of("runtime-dependencies.txt", "runtime-classpath.txt", "test-dependencies.txt"))
            write(build.resolve(file), "packaged");
        for (String release : List.of("17", "21")) {
            write(target.resolve("consumer-evidence/outer-release.properties"), "consumer.release=" + release);
            String evidence = "result=PASS\nscenario=scenario\nconsumer.fixture=fixture\nconsumer.release=" + release
                    + "\nconsumer.compiler.release=" + release;
            Path path = build.resolve("consumer-evidence/scenario.properties");
            write(path, evidence); ConsumerHarness.audit(release);
            for (String field : List.of("consumer.release", "consumer.compiler.release")) {
                write(path, evidence.replace(field + "=" + release, field + "=invalid"));
                fails(field + " differs from selected release", () -> ConsumerHarness.audit(release));
            }
            write(path, evidence);
            write(target.resolve("consumer-evidence/outer-release.properties"), "consumer.release=invalid");
            fails("Outer consumer.release differs", () -> ConsumerHarness.audit(release));
        }
    }

    static void write(Path path, String value) throws Exception {
        Files.createDirectories(path.getParent()); Files.writeString(path, value);
    }
}
