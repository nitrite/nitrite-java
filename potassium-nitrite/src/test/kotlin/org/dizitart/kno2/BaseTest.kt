/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.kno2

import org.dizitart.no2.Nitrite
import org.junit.After
import org.junit.Assert
import java.io.File
import java.util.*

/**
 *
 * @author Anindya Chatterjee
 */
fun getRandomTempDbFile(): String {
    val dataDir = System.getProperty("java.io.tmpdir") +
        File.separator + "nitrite" + File.separator + "data"
    val file = File(dataDir)
    if (!file.exists()) {
        Assert.assertTrue(file.mkdirs())
    }
    return file.path + File.separator + UUID.randomUUID().toString() + ".db"
}

abstract class BaseTest {
    val fileName = getRandomTempDbFile()
    var db: Nitrite? = null

    @After
    fun clear() {
        if (db != null) {
            db?.commit()
            db?.close()
        }
        val file = File(fileName)
        if (file.exists()) file.delete()
    }
}