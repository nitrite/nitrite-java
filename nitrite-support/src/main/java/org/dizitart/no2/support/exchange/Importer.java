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

package org.dizitart.no2.support.exchange;

import com.fasterxml.jackson.core.JsonParser;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.ValidationException;

import java.io.*;

import static org.dizitart.no2.common.util.ValidationUtils.notNull;
import static org.dizitart.no2.support.exchange.Exporter.createObjectMapper;

/**
 * The Importer class provides methods to import data from a file or stream into
 * Nitrite database.
 * <p>
 * It uses the provided ImportOptions to configure the import process.
 * 
 * @author Anindya Chatterjee
 * @since 1.0
 */
public class Importer {
    private ImportOptions options;

    private Importer() {
    }

    /**
     * Creates a new instance of {@link Importer} with the specified import options.
     *
     * @param importOptions the import options to use
     * @return a new instance of {@link Importer} with the specified import options
     * @throws ValidationException if the import options or nitrite factory is null
     */
    public static Importer withOptions(ImportOptions importOptions) {
        Importer importer = new Importer();
        notNull(importOptions, "importOptions cannot be null");
        notNull(importOptions.getNitriteFactory(), "nitriteFactory cannot be null");

        if (importOptions.getJsonFactory() == null) {
            importOptions.setJsonFactory(createObjectMapper().getFactory());
        }

        importer.options = importOptions;
        return importer;
    }

    /**
     * Imports data from the specified file.
     *
     * @param file the file to import data from
     */
    public void importFrom(String file) {
        importFrom(new File(file));
    }

    /**
     * Imports data from a file.
     *
     * @param file the file to import data from
     * @throws NitriteIOException if there is an I/O error while reading content from the file
     */
    public void importFrom(File file) {
        try (FileInputStream stream = new FileInputStream(file)) {
            importFrom(stream);
        } catch (IOException ioe) {
            throw new NitriteIOException("I/O error while reading content from file " + file, ioe);
        }
    }

    /**
     * Imports data from the specified input stream.
     *
     * @param stream the input stream to import data from
     * @throws IOException if an I/O error occurs
     */
    public void importFrom(InputStream stream) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(stream)) {
            importFrom(reader);
        }
    }

    /**
     * Imports data from a Reader object using a JSON parser.
     *
     * @param reader the Reader object to import data from
     * @throws NitriteIOException if there is an I/O error while creating the parser from the reader or while importing data
     */
    public void importFrom(Reader reader) {
        JsonParser parser;
        try {
            parser = options.getJsonFactory().createParser(reader);
        } catch (IOException ioe) {
            throw new NitriteIOException("I/O error while creating parser from reader", ioe);
        }

        if (parser != null) {
            NitriteJsonImporter jsonImporter = new NitriteJsonImporter();
            jsonImporter.setParser(parser);
            jsonImporter.setOptions(options);
            try {
                jsonImporter.importData();
            } catch (IOException | ClassNotFoundException e) {
                throw new NitriteIOException("Error while importing data", e);
            }
        }
    }
}
