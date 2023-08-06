/*
 * Copyright (c) 2019-2020. Nitrite author or authors.
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

package org.dizitart.no2.mvstore;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.h2.mvstore.MVStoreTool;
import org.h2.store.fs.FilePath;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.h2.mvstore.MVStoreTool.info;
import static org.h2.mvstore.MVStoreTool.rollback;

/**
 * The nitrite database recovery utility.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
@Slf4j
public class Recovery {

    /**
     * Attempt a database file recovery by rolling back to the
     * newest good version.
     *
     * @param fileName the database file name
     */
    public static void recover(String fileName) {
        StringWriter messages = new StringWriter();
        try {
            PrintWriter pw = new PrintWriter(messages);
            long version = Long.MAX_VALUE;
            boolean success = false;
            while (version >= 0) {
                pw.println(version == Long.MAX_VALUE ? "Trying latest version"
                        : ("Trying version " + version));
                pw.flush();

                version = rollback(fileName, version, messages);
                try {
                    String error = info(fileName + ".temp", messages);
                    if (error == null) {
                        FilePath.get(fileName).moveTo(FilePath.get(fileName + ".back"), true);
                        FilePath.get(fileName + ".temp").moveTo(FilePath.get(fileName), true);
                        pw.println("Success");
                        success = true;
                        break;
                    }
                    pw.println("... failed: " + error);
                } catch (Exception e) {
                    pw.println("Fail: " + e.getMessage());
                    pw.flush();
                }
                version--;
            }
            pw.flush();
            pw.close();

            if (!success) {
                throw new NitriteIOException("Failed to repair database with log: " + messages);
            }
        } catch (NitriteIOException e) {
            throw e;
        } catch (Exception e) {
            throw new NitriteIOException("Failed to repair database with log: " + messages, e);
        }
    }
}
