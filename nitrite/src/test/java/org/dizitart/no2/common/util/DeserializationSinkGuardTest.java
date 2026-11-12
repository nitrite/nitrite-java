package org.dizitart.no2.common.util;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

/**
 * CWE-502 guard (GHSA-9297-g93h-86gg, GHSA-7w7v-2j76-mqwp, GHSA-q86x-r2q3-vf4p,
 * GHSA-463g-rx95-c9fp): every native java deserialization sink in main sources must
 * go through an allowlist. A new sink fails this test until it is filtered
 * (use {@link ObjectUtils#createFilteredInputStream}) and its file reviewed and listed below.
 */
public class DeserializationSinkGuardTest {
    private static final Pattern SINK = Pattern.compile(
        "new ObjectInputStream\\s*\\(|extends ObjectInputStream|\\bJavaSerializer\\b|\\bExternalizableSerializer\\b"
            + "|XMLDecoder|readUnshared\\s*\\(");

    // reviewed: each of these installs an allowlist, or only reads bytes it just wrote itself
    private static final Set<String> REVIEWED = new HashSet<>(Arrays.asList(
        "ObjectUtils.java",                // createFilteredInputStream; deepCopy reads its own bytes
        "FilteredObjectDataType.java",     // mvstore, JEP 290 filter
        "NitriteObjectInputStream.java",   // legacy v1 migration, JEP 290 filter
        "NitriteSerializers.java"          // rocksdb FilteredJavaSerializer
    ));

    @Test
    public void testNoUnreviewedDeserializationSink() throws IOException {
        Path root = Paths.get("").toAbsolutePath().getParent();
        List<String> offenders;
        try (Stream<Path> files = Files.walk(root)) {
            offenders = files
                .filter(p -> p.toString().endsWith(".java") || p.toString().endsWith(".kt"))
                .filter(p -> p.toString().contains("/src/main/"))
                .filter(p -> !REVIEWED.contains(p.getFileName().toString()))
                .filter(DeserializationSinkGuardTest::hasSink)
                .map(p -> root.relativize(p).toString())
                .collect(Collectors.toList());
        }
        assertTrue("Unfiltered deserialization sink(s): " + offenders, offenders.isEmpty());
    }

    private static boolean hasSink(Path path) {
        try {
            return SINK.matcher(new String(Files.readAllBytes(path))).find();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
