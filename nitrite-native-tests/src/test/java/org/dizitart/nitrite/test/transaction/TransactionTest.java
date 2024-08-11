package org.dizitart.nitrite.test.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import org.dizitart.nitrite.test.DatabaseTestUtils;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.transaction.Session;
import org.dizitart.no2.transaction.Transaction;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TransactionTest {

    @Test
    void transactionCommitShouldSucceed() throws IOException {

        try (final Nitrite database = DatabaseTestUtils.setupDatabase()) {

            final var testCollection = database.getCollection("test");

            try (final Session session = database.createSession();
                 final Transaction transaction = session.beginTransaction()) {

                final Document expectedDocument = Document.createDocument()
                    .put("test", "test");

                final NitriteId id = transaction.getCollection("test").insert(expectedDocument).iterator().next();

                assertThat(testCollection.getById(id)).isNull();

                transaction.commit();

                final Document actualDocument = testCollection.getById(id);

                assertThat(actualDocument).isNotNull();
                assertThat(actualDocument.getId()).isEqualTo(id);
                assertThat(actualDocument.get("test", String.class)).isEqualTo("test");
            }
        }
    }

    @Test
    void transactionRollbackShouldSucceed() throws IOException {

        try (final Nitrite database = DatabaseTestUtils.setupDatabase()) {

            final var testCollection = database.getCollection("test");

            try (final Session session = database.createSession();
                 final Transaction transaction = session.beginTransaction()) {

                final Document expectedDocument = Document.createDocument()
                    .put("test", "test");

                final NitriteId id = transaction.getCollection("test").insert(expectedDocument).iterator().next();

                assertThat(testCollection.getById(id)).isNull();

                transaction.rollback();

                assertThat(testCollection.getById(id)).isNull();
            }
        }
    }
}
