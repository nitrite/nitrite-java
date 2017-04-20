/*
 * Copyright 2017 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
