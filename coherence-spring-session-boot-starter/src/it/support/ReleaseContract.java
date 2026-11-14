/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package consumer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;

/** Selected compiler target checks; shared only by consumer tests. */
public final class ReleaseContract {
    private ReleaseContract() { }

    public record Target(int release) {
        public Target {
            if (release != 17 && release != 21) throw new AssertionError("Unsupported consumer release: " + release);
        }
        public int major() { return release + 44; }
    }

    public static Target selected(String declared, String compiler, String outer) {
        if (!"17".equals(declared) && !"21".equals(declared))
            throw new AssertionError("Invalid consumer.release: " + declared + "; expected 17 or 21");
        if (!declared.equals(compiler))
            throw new AssertionError("consumer.compiler.release " + compiler + " differs from consumer.release " + declared);
        if (!declared.equals(outer))
            throw new AssertionError("Outer consumer.release " + outer + " differs from consumer.release " + declared);
        Target target = new Target(Integer.parseInt(declared));
        if (Runtime.version().feature() < target.release())
            throw new AssertionError("Runtime Java " + Runtime.version().feature() + " cannot run consumer release " + declared);
        return target;
    }

    public static int directory(Path directory, Target target) throws Exception {
        int count = 0;
        try (var files = Files.walk(directory)) {
            for (Path path : files.filter(p -> p.toString().endsWith(".class")).toList()) {
                check(Files.readAllBytes(path), target, path.toString());
                count++;
            }
        }
        if (count == 0) throw new AssertionError("No compiled consumer classes: " + directory);
        return count;
    }

    public static void jar(Path jar, Target target) throws Exception {
        try (var file = new JarFile(jar.toFile())) {
            for (var entry : file.stream().filter(e -> e.getName().endsWith(".class")).toList()) {
                try (var in = file.getInputStream(entry)) {
                    check(in.readAllBytes(), target, jar + "!" + entry.getName());
                }
            }
        }
    }

    private static void check(byte[] bytes, Target target, String origin) {
        if (bytes.length < 8 || bytes[0] != (byte) 0xca || bytes[1] != (byte) 0xfe
                || bytes[2] != (byte) 0xba || bytes[3] != (byte) 0xbe)
            throw new AssertionError("Invalid class file: " + origin);
        int actual = (bytes[6] & 255) * 256 + (bytes[7] & 255);
        if (actual != target.major())
            throw new AssertionError(origin + ": expected release " + target.release() + " / major "
                    + target.major() + ", actual major " + actual);
    }
}
