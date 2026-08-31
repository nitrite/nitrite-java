package org.dizitart.example;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.mvstore.MVStoreModule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A plain desktop application with a Nitrite database in it, browsable from Fanlight.
 *
 * <pre>
 *   mvn package
 *   java -Ddbinspect.bridge.enabled=true -jar target/nitrite-bridge-example.jar
 * </pre>
 *
 * <p>Without the system property it starts, opens its database and says the bridge is off — which
 * is the release guard, not an error. Every reference to the bridge lives in {@link Inspector}, so
 * a build that ships without the artifact loads this class and never loads that one.
 */
public final class ExampleApp {

    /** The size the page-latency budget is written against: < 150 ms per page on 50k rows. */
    private static final int DEFAULT_ROWS = 50_000;

    private ExampleApp() {}

    public static void main(String[] arguments) throws Exception {
        File file = new File(arguments.length > 0 ? arguments[0] : "example.db");
        int rows = arguments.length > 1 ? Integer.parseInt(arguments[1]) : DEFAULT_ROWS;

        Nitrite db =
                Nitrite.builder()
                        .loadModule(MVStoreModule.withConfig().filePath(file).build())
                        .openOrCreate();
        try {
            seed(db, rows);
            System.err.println(
                    "example app: " + file.getAbsolutePath() + ", " + rows + " rows in \"users\"");
            Inspector.start(db);
            // A desktop application would now show its window. There is nothing to show, so it
            // waits — the bridge is what the developer came for.
            Thread.currentThread().join();
        } finally {
            db.close();
        }
    }

    private static void seed(Nitrite db, int rows) {
        NitriteCollection users = db.getCollection("users");
        if (users.size() >= rows) {
            return;
        }
        users.clear();

        Random random = new Random(7);
        byte[] avatar = new byte[100 * 1024];
        random.nextBytes(avatar);

        // In batches, because one insert per row across 50k rows is minutes of commits rather
        // than seconds and this is the first thing anybody runs.
        List<Document> batch = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            Document row =
                    Document.createDocument()
                            .put("id", i)
                            .put("name", i == 3 ? "user with a ünicode name" : "user " + i)
                            .put("score", i / 3.0);
            if (i % 2 == 0) {
                row.put("age", 20 + (i % 50));
            }
            if (i == 0) {
                row.put("avatar", avatar);
            }
            batch.add(row);
            if (batch.size() == 1000) {
                users.insert(batch.toArray(new Document[0]));
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            users.insert(batch.toArray(new Document[0]));
        }
        db.commit();
    }
}
