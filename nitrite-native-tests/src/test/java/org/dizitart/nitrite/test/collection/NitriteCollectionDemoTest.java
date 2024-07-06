package org.dizitart.nitrite.test.collection;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;

import org.dizitart.nitrite.test.DatabaseTestUtils;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.junit.jupiter.api.Test;

public class NitriteCollectionDemoTest {

    @Test
    void readAndWriteOperationsShouldSucceed() throws IOException {

        try (final Nitrite database = DatabaseTestUtils.setupDatabase()) {

            final var testCollection = database.getCollection("test");

            final Document expectedDocument = Document.createDocument()
                .put("test", "test")
                // requires additional serialization hints for BigDecimal and BigInteger
                // this also applies to other Serializable classes
                // see: src/test/resources/META-INF/native-image/org.dizitart/nitrite-native-tests/serialization-config.json
                .put("price", new BigDecimal("9.99"));

            final NitriteId id = testCollection.insert(expectedDocument).iterator().next();

            final Document actualDocument = testCollection.getById(id);

            assertThat(actualDocument).isNotNull();
            assertThat(actualDocument.getId()).isEqualTo(id);
            assertThat(actualDocument.get("test", String.class)).isEqualTo("test");
            assertThat(actualDocument.get("price", BigDecimal.class)).isEqualTo("9.99");
        }
    }
}
