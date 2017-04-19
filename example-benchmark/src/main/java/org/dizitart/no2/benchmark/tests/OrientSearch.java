package org.dizitart.no2.benchmark.tests;

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.dizitart.no2.benchmark.data.Person;

import java.util.List;

/**
 * @author Anindya Chatterjee.
 */
public class OrientSearch extends BaseOrientBenchmark {

    @Override
    public void beforeTest() {
        super.beforeTest();
        ODatabaseRecordThreadLocal.INSTANCE.set(db.getUnderlying());
        db.command(new OCommandSQL("CREATE INDEX firstName ON Person (firstName) NOTUNIQUE"));
        db.command(new OCommandSQL("CREATE INDEX personalNote ON Person (personalNote) FULLTEXT"));
    }

    @Override
    public void beforeRun() {
        ODatabaseRecordThreadLocal.INSTANCE.set(db.getUnderlying());
        for (Person person : personList) {
            db.save(person);
        }
    }

    @Override
    public void runTest() {
        List<Person> result = db.query(
                new OSQLSynchQuery<Person>("select * from Person where firstName = 'abcd'"));
        assert result != null;

        result = db.query(
                new OSQLSynchQuery<Person>("select * from Person where personalNote CONTAINSTEXT 'Lorem'"));
        assert result != null;
    }
}
