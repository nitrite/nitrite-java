package org.dizitart.no2.benchmark.tests;

import org.dizitart.no2.benchmark.data.Person;

/**
 * @author Anindya Chatterjee.
 */
public class NitriteInsert extends BaseNitriteBenchmark {

    @Override
    public void beforeRun() {
    }

    @Override
    public void runTest() {
        for (Person person : personList) {
            personRepository.insert(person);
        }
    }
}
