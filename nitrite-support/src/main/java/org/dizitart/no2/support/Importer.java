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

package org.dizitart.no2.support;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.exceptions.NitriteIOException;

import java.io.*;

import static org.dizitart.no2.support.Exporter.createObjectMapper;


/**
 * Nitrite database import utility. It imports data from
 * a json file. Contents of a Nitrite database can be imported
 * using this tool.
 * <p>
 * [[app-listing]]
 * include::/src/docs/asciidoc/tools/data-format.adoc[]
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
public class Importer {
    private Nitrite db;
    private JsonFactory jsonFactory;

    private Importer() {
    }

    /**
     * Creates a new {@link Importer} instance.
     *
     * @param db the db
     * @return the importer instance
     */
    public static Importer of(Nitrite db) {
        return of(db, createObjectMapper());
    }

    public static Importer of(Nitrite db, ObjectMapper objectMapper) {
        Importer importer = new Importer();
        importer.db = db;
        importer.jsonFactory = objectMapper.getFactory();
        return importer;
    }

    /**
     * Imports data from a file path.
     *
     * @param file the file path
     */
    public void importFrom(String file) {
        importFrom(new File(file));
    }

    /**
     * Imports data from a file.
     *
     * @param file the file
     * @throws NitriteIOException if there is any low-level I/O error.
     */
    public void importFrom(File file) {
        try (FileInputStream stream = new FileInputStream(file)) {
            importFrom(stream);
        } catch (IOException ioe) {
            throw new NitriteIOException("I/O error while reading content from file " + file, ioe);
        }
    }

    /**
     * Imports data from an {@link InputStream}.
     *
     * @param stream the stream
     */
    public void importFrom(InputStream stream) {
        importFrom(new InputStreamReader(stream));
    }

    /**
     * Imports data from a {@link Reader}.
     *
     * @param reader the reader
     * @throws NitriteIOException if there is any error while reading the data.
     */
    public void importFrom(Reader reader) {
        JsonParser parser;
        try {
            parser = jsonFactory.createParser(reader);
        } catch (IOException ioe) {
            throw new NitriteIOException("I/O error while creating parser from reader", ioe);
        }

        if (parser != null) {
            NitriteJsonImporter jsonImporter = new NitriteJsonImporter(db);
            jsonImporter.setParser(parser);
            try {
                jsonImporter.importData();
            } catch (IOException | ClassNotFoundException e) {
                throw new NitriteIOException("error while importing data", e);
            }
        }
    }
}
