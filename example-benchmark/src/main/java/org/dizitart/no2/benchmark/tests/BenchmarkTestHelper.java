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

import org.dizitart.no2.benchmark.data.Generator;
import org.dizitart.no2.benchmark.data.Person;

import java.io.File;

/**
 * @author Anindya Chatterjee.
 */
class BenchmarkTestHelper {
    Person[] loadData() {
        return Generator.getInstance().create(1000);
    }

    void deleteDir(String dirName) {
        File file = new File(dirName);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    if (child.isDirectory()) {
                        deleteDir(child.getAbsolutePath());
                    } else {
                        child.delete();
                    }
                }
                file.delete();
            } else {
                file.delete();
            }
        }
    }
}
