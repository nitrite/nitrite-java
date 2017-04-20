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
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.dizitart.no2.benchmark.core.Benchmark;
import org.dizitart.no2.benchmark.data.Address;
import org.dizitart.no2.benchmark.data.Person;
import org.dizitart.no2.benchmark.data.PrivateData;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Anindya Chatterjee.
 */
public abstract class BaseOrientBenchmark implements Benchmark {
    OObjectDatabaseTx db;
    Person[] personList;
    BenchmarkTestHelper testHelper = new BenchmarkTestHelper();

    @Override
    public void beforeTest() {
        try {
            personList = testHelper.loadData();
            testHelper.deleteDir(System.getProperty("java.io.tmpdir") + "/orientdb/");
            Files.createDirectory(Paths.get(System.getProperty("java.io.tmpdir") + "/orientdb/"));
            db = new OObjectDatabaseTx("plocal:" +
                    System.getProperty("java.io.tmpdir") + "/orientdb/person");
            if (db.exists()) {
                db.open("admin", "admin");
                db.drop();
            }
            db.create();
            db.getEntityManager().registerEntityClass(Person.class);
            db.getEntityManager().registerEntityClass(Address.class);
            db.getEntityManager().registerEntityClass(PrivateData.class);
        } catch (Throwable e) {
            System.out.println("error in creating db ");
            e.printStackTrace();
        }
    }

    @Override
    public void afterRun() {
        ODatabaseRecordThreadLocal.INSTANCE.set(db.getUnderlying());
        db.command(new OCommandSQL("TRUNCATE CLASS Person")).execute();
    }

    @Override
    public void afterTest() {
        if (db != null) {
            ODatabaseRecordThreadLocal.INSTANCE.set(db.getUnderlying());
            db.commit();
            db.close();
        }
        testHelper.deleteDir(System.getProperty("java.io.tmpdir") + "/orientdb/");
    }
}
