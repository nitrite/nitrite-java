package org.dizitart.example;

import org.dizitart.dbinspect.BridgeAdapter;
import org.dizitart.dbinspect.BridgeServer;
import org.dizitart.dbinspect.DbInspect;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.bridge.NitriteAdapter;

import java.util.Collections;
import java.util.List;

/**
 * Everything this application knows about dbinspect, in one class that nothing else references.
 *
 * <p><b>That is the whole design.</b> The bridge is a development dependency: the shipped build
 * declares it {@code provided} and the artifact contains not one class of it (THREAT-MODEL §7
 * criterion 2, and {@code tool/verify_release_jar.sh} greps the built jar for exactly that). A JVM
 * only loads a class when something touches it, so keeping every reference behind this one door
 * means the release build loads {@link ExampleApp} happily and never opens the door — no
 * reflection, no build-time code generation, no {@code if (DEBUG)} that still has to link.
 *
 * <p>The second guard is {@code -Ddbinspect.bridge.enabled=true}. Two signals, and neither one
 * survives into a production launch by accident.
 */
final class Inspector {

    private Inspector() {}

    static void start(Nitrite db) {
        try {
            open(db);
        } catch (NoClassDefFoundError absent) {
            // The release build. Not an error: the artifact was built without the bridge, which
            // is the guard working.
            System.err.println("dbinspect is not in this build");
        } catch (Exception failed) {
            System.err.println("dbinspect did not start: " + failed);
        }
    }

    private static void open(Nitrite db) throws Exception {
        List<BridgeAdapter> adapters =
                Collections.singletonList(
                        NitriteAdapter.builder(db, "nitrite-main", "example.db").build());
        BridgeServer bridge = DbInspect.start(DbInspect.options("example_app", adapters));
        if (bridge == null) {
            // bridgeEnabled() said no, and has already logged how to say yes.
            return;
        }
        // One machine-readable line, so tool/measure_page_latency.dart can be pointed at this
        // without parsing the pairing banner. The banner itself follows, for a human.
        System.out.println(
                String.format(
                        "{\"host\":\"%s\",\"port\":%d,\"code\":\"%s\"}",
                        bridge.address().getHostAddress(),
                        bridge.port(),
                        bridge.pairingCode().value()));
        System.out.println(bridge.banner());
        System.out.flush();
    }
}
