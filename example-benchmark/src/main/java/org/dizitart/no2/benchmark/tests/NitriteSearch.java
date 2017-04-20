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
