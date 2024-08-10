package org.dizitart.nitrite.test.compat;

import static org.assertj.core.api.Assertions.assertThat;
import org.dizitart.nitrite.test.DatabaseTestUtils;
import org.dizitart.no2.Nitrite;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

public class CompatTest {

    @Test
    void shouldMigrateV3DatabaseToCurrentVersion() throws IOException {

        // loading a resource in native image requires it to be included in resource-config.json
        // the current pattern allows loading all resources that end with .db
        final URL oldDb = CompatTest.class.getResource("/no2-v3.db");
        try (final Nitrite database = DatabaseTestUtils.setupDatabase(oldDb)) {

            final var testCollection = database.getCollection("test");

            final var document = testCollection.find().firstOrNull();

            assertThat(document).isNotNull();
            assertThat(document.get("firstName")).isEqualTo("John");
            assertThat(document.get("lastName")).isEqualTo("Doe");
        }
    }
}
