package org.dizitart.nitrite.test.serialization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.dizitart.nitrite.test.DatabaseTestUtils;
import org.dizitart.nitrite.test.serialization.model.UntrustedPayload;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SerialFilterTest {

    private static final String ALLOWED_CLASSES = "org.dizitart.nitrite.test.serialization.model.*";

    @Test
    void rejectedClassShouldBeReportedWithoutRollback() throws IOException {

        final Path databasePath = DatabaseTestUtils.getRandomDatabasePath();

        try (final Nitrite database = DatabaseTestUtils.openDatabase(databasePath, null)) {
            // requires additional serialization hints for UntrustedPayload
            // see: src/test/resources/META-INF/native-image/org.dizitart/nitrite-native-tests/serialization-config.json
            database.getCollection("test").insert(Document.createDocument()
                .put("payload", new UntrustedPayload("test")));
        }

        // the error message is built from the rejected class (name and package),
        // which has to work in native image as well
        try (final Nitrite database = DatabaseTestUtils.openDatabase(databasePath, null)) {
            assertThatThrownBy(() -> database.getCollection("test").find().toList())
                .hasStackTraceContaining("Class " + UntrustedPayload.class.getName() + " is not allowed")
                .hasStackTraceContaining("allowedClasses(\"org.dizitart.nitrite.test.serialization.model.**\")");
        }

        // a rejected class is no corruption, so the committed data must still be there
        try (final Nitrite database = DatabaseTestUtils.openDatabase(databasePath, ALLOWED_CLASSES)) {

            final var testCollection = database.getCollection("test");

            assertThat(testCollection.size()).isEqualTo(1);
            final Document document = testCollection.find().firstOrNull();
            assertThat(document).isNotNull();
            assertThat(document.get("payload", UntrustedPayload.class).getValue()).isEqualTo("test");
        }
    }

    @Test
    void rejectedArrayShouldBeReportedByComponentType() throws IOException {

        final Path databasePath = DatabaseTestUtils.getRandomDatabasePath();

        try (final Nitrite database = DatabaseTestUtils.openDatabase(databasePath, null)) {
            // object arrays are only stored through java serialization when nested, e.g. in a list
            final List<Object> payloads = new ArrayList<>();
            payloads.add(new UntrustedPayload[]{new UntrustedPayload("test")});
            database.getCollection("test").insert(Document.createDocument()
                .put("payloads", payloads));
        }

        // the message has to name the component type instead of the array type
        try (final Nitrite database = DatabaseTestUtils.openDatabase(databasePath, null)) {
            assertThatThrownBy(() -> database.getCollection("test").find().toList())
                .hasStackTraceContaining("Class " + UntrustedPayload.class.getName() + " is not allowed")
                .hasStackTraceContaining("allowedClasses(\"org.dizitart.nitrite.test.serialization.model.**\")");
        }

        try (final Nitrite database = DatabaseTestUtils.openDatabase(databasePath, ALLOWED_CLASSES)) {

            final Document document = database.getCollection("test").find().firstOrNull();

            assertThat(document).isNotNull();
            final List<?> payloads = document.get("payloads", List.class);
            assertThat(payloads).hasSize(1);
            final UntrustedPayload[] array = (UntrustedPayload[]) payloads.get(0);
            assertThat(array).hasSize(1);
            assertThat(array[0].getValue()).isEqualTo("test");
        }
    }
}
