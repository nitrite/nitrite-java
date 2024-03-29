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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dizitart.no2.exceptions.NitriteIOException;

import java.io.*;

import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * The Exporter class provides methods to export Nitrite database data to a file
 * or an output stream in JSON format.
 * <p>
 * It uses the provided ExportOptions to configure the export process.
 * 
 * @author Anindya Chatterjee
 * @since 1.0
 */
public class Exporter {
    private ExportOptions options;

    private Exporter() {
    }

    /**
     * Creates an Exporter instance with the specified export options.
     *
     * @param exportOptions the export options to be set
     *                      (must not be null and must have a valid nitrite factory)
     *
     * @return the Exporter instance with the specified export options
     */
    public static Exporter withOptions(ExportOptions exportOptions) {
        Exporter exporter = new Exporter();
        notNull(exportOptions, "exportOptions cannot be null");
        notNull(exportOptions.getNitriteFactory(), "nitriteFactory cannot be null");

        if (exportOptions.getJsonFactory() == null) {
            exportOptions.setJsonFactory(createObjectMapper().getFactory());
        }

        exporter.options = exportOptions;
        return exporter;
    }

    /**
     * Creates and returns an instance of ObjectMapper with custom configurations.
     *
     * @return an instance of ObjectMapper with custom configurations.
     */
    public static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setVisibility(
                objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));
        return objectMapper;
    }

    /**
     * Exports the data to the specified file.
     *
     * @param file the file to export the data to
     */
    public void exportTo(String file) {
        exportTo(new File(file));
    }

    /**
     * Exports the content to the specified file.
     *
     * @param file the file to export the content to
     * @throws NitriteIOException if there is an I/O error while writing content to the file
     */
    public void exportTo(File file) {
        try {
            if (file.isDirectory()) {
                throw new IOException(file.getPath() + " is not a file");
            }

            File parent = file.getParentFile();
            // if parent dir does not exist, try to create it
            if (!parent.exists()) {
                boolean result = parent.mkdirs();
                if (!result) {
                    throw new IOException("Failed to create parent directory " + parent.getPath());
                }
            }
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                exportTo(outputStream);
            }
        } catch (IOException ioe) {
            throw new NitriteIOException("I/O error while writing content to file " + file, ioe);
        }
    }

    /**
     * Exports the data to the specified output stream.
     *
     * @param stream the output stream to export the data to
     * @throws IOException if an I/O error occurs
     */
    public void exportTo(OutputStream stream) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            exportTo(writer);
        }
    }

    /**
     * Exports the data to the specified writer using JSON format.
     *
     * @param writer the writer to export the data to
     * @throws NitriteIOException if there is an I/O error while writing data with writer
     * @throws NitriteIOException if there is an error while exporting data
     */
    public void exportTo(Writer writer) {
        JsonGenerator generator;
        try {
            generator = options.getJsonFactory().createGenerator(writer);
            generator.setPrettyPrinter(new DefaultPrettyPrinter());
        } catch (IOException ioe) {
            throw new NitriteIOException("I/O error while writing data with writer", ioe);
        }

        NitriteJsonExporter jsonExporter = new NitriteJsonExporter();
        jsonExporter.setGenerator(generator);
        jsonExporter.setOptions(options);
        try {
            jsonExporter.exportData();
        } catch (IOException | ClassNotFoundException e) {
            throw new NitriteIOException("Error while exporting data", e);
        }
    }
}
