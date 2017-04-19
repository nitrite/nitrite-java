package org.dizitart.no2.benchmark.tests;

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import org.dizitart.no2.benchmark.data.Person;

/**
 * @author Anindya Chatterjee.
 */
public class OrientInsert extends BaseOrientBenchmark {

    @Override
    public void beforeRun() {

    }

    @Override
    public void runTest() {
        ODatabaseRecordThreadLocal.INSTANCE.set(db.getUnderlying());
        for (Person person : personList) {
            db.save(person);
        }
    }
}
