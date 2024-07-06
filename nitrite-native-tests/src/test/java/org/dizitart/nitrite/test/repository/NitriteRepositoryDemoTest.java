package org.dizitart.nitrite.test.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.dizitart.nitrite.test.DatabaseTestUtils;
import org.dizitart.nitrite.test.repository.model.Person;
import org.dizitart.nitrite.test.repository.model.Title;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.filters.FluentFilter;
import org.junit.jupiter.api.Test;

public class NitriteRepositoryDemoTest {

    @Test
    void readAndWriteOperationsShouldSucceed() throws IOException {

        try (final Nitrite database = DatabaseTestUtils.setupDatabase()) {

            final var personRepository = database.getRepository(Person.class);

            final Person expectedPerson = new Person(
                UUID.randomUUID().toString(),
                "Testi",
                "Tester",
                20,
                Set.of(Title.PROF),
                false
            );
            personRepository.insert(expectedPerson);

            final Person actualPerson = personRepository.getById(expectedPerson.getId());

            assertThat(actualPerson).isEqualTo(expectedPerson);
        }
    }

    @Test
    void filterByIndexedFieldShouldSucceed() throws IOException {

        try (final Nitrite database = DatabaseTestUtils.setupDatabase()) {

            final var personRepository = database.getRepository(Person.class);

            final Person expectedPerson = new Person(
                UUID.randomUUID().toString(),
                "Testi",
                "Tester",
                20,
                Set.of(Title.PROF),
                true
            );
            personRepository.insert(expectedPerson);

            final Person actualPerson = personRepository.find(
                FluentFilter.where("lastName").eq("Tester")
            ).firstOrNull();

            assertThat(actualPerson).isEqualTo(expectedPerson);
        }
    }
}
