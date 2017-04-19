package org.dizitart.no2.benchmark.tests;

import org.dizitart.no2.IndexOptions;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.benchmark.data.Person;
import org.dizitart.no2.objects.Cursor;

import static org.dizitart.no2.objects.filters.ObjectFilters.eq;
import static org.dizitart.no2.objects.filters.ObjectFilters.text;

/**
 * @author Anindya Chatterjee.
 */
public class NitriteSearch extends BaseNitriteBenchmark {

    @Override
    public void beforeTest() {
        super.beforeTest();
        personRepository.createIndex("firstName", IndexOptions.indexOptions(IndexType.NonUnique));
        personRepository.createIndex("personalNote", IndexOptions.indexOptions(IndexType.Fulltext));
    }

    @Override
    public void beforeRun() {
        for (Person person : personList) {
            personRepository.insert(person);
        }
    }

    @Override
    public void runTest() {
        Cursor cursor = personRepository.find(eq("firstName", "abcd"));
        assert cursor != null;

        cursor = personRepository.find(text("personalNote", "Lorem"));
        assert cursor != null;
    }
}
