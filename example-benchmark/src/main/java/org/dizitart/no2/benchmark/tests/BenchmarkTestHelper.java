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
