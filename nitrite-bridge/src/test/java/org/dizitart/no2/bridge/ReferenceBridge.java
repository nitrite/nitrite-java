package org.dizitart.no2.bridge;

import org.dizitart.dbinspect.BridgeAdapter;
import org.dizitart.dbinspect.BridgeServer;
import org.dizitart.dbinspect.DbInspect;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.rocksdb.RocksDBModule;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

/**
 * A Nitrite-backed bridge you can point the conformance suite at.
 *
 * <pre>
 *   mvn -f nitrite-bridge/pom.xml test-compile
 *   tool/run_reference_bridge.sh [memory|mvstore|rocksdb]
 *   dart run .../conformance/bin/dbinspect_conformance.dart 127.0.0.1:&lt;port&gt; &lt;code&gt;
 * </pre>
 *
 * <p>It prints one line of JSON — {@code {"host":…,"port":…,"code":…}} — before the bridge's own
 * pairing banner, so a script does not have to parse the banner. Same shape as the Dart and
 * engine-neutral JVM reference bridges, because the runner takes a {@code host:port} and a pairing
 * code and nothing else.
 *
 * <p>The adapter is constructed with <b>no options</b>, because that is what
 * {@code docs/THREAT-MODEL.md} §7 criterion 10 is about. It lives in test sources for the same
 * reason the fixtures do — it is not part of the published artifact.
 */
public final class ReferenceBridge {

    private ReferenceBridge() {}

    static Nitrite open(String engine) throws Exception {
        switch (engine) {
            case "memory":
                return Fixtures.memoryDb();
            case "mvstore":
                return Nitrite.builder()
                        .loadModule(MVStoreModule.withConfig().filePath(temp("mvstore")).build())
                        .registerEntityConverter(new Fixtures.Note.NoteConverter())
                        .openOrCreate();
            case "rocksdb":
                return Nitrite.builder()
                        .loadModule(RocksDBModule.withConfig().filePath(temp("rocksdb")).build())
                        .registerEntityConverter(new Fixtures.Note.NoteConverter())
                        .openOrCreate();
            default:
                throw new IllegalArgumentException(
                        "unknown engine \"" + engine + "\"; use memory, mvstore or rocksdb");
        }
    }

    private static File temp(String engine) throws Exception {
        File directory = Files.createTempDirectory("dbinspect-" + engine).toFile();
        directory.deleteOnExit();
        return new File(directory, "app.db");
    }

    public static void main(String[] arguments) throws Exception {
        // The reference bridge is the one place the desktop guard is switched on deliberately.
        System.setProperty(DbInspect.ENABLE_PROPERTY, "true");

        Nitrite db = Fixtures.fill(open(arguments.length > 0 ? arguments[0] : "memory"));
        ObjectRepository<Fixtures.Note> notes = db.getRepository(Fixtures.Note.class);
        notes.insert(new Fixtures.Note("a note", 12, "ada"));

        List<BridgeAdapter> adapters =
                Collections.singletonList(
                        NitriteAdapter.builder(db, "nitrite-main", "app.db")
                                .repositories(notes)
                                .build());
        BridgeServer bridge = DbInspect.start(DbInspect.options("reference_bridge", adapters));
        if (bridge == null) {
            System.err.println("this build does not contain the bridge — see DbInspect#bridgeEnabled");
            System.exit(70); // EX_SOFTWARE
        }

        System.out.println(
                String.format(
                        "{\"host\":\"%s\",\"port\":%d,\"code\":\"%s\"}",
                        bridge.address().getHostAddress(),
                        bridge.port(),
                        bridge.pairingCode().value()));
        System.out.println(bridge.banner());
        System.out.flush();

        // Nothing to do but stay up. The suite kills the process when it is finished, and it has
        // to be a fresh process for the next run anyway — the last phase closes pairing for good.
        Thread.currentThread().join();
    }
}
