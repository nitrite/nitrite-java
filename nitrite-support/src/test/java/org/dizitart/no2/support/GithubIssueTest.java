package org.dizitart.no2.support;

import lombok.Data;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.common.mapper.JacksonMapperModule;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.dizitart.no2.repository.Cursor;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.annotations.Id;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import static org.dizitart.no2.support.BaseExternalTest.getRandomTempDbFile;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GithubIssueTest {

    @Test
    public void testIssue819() throws IOException {
        String initialDbPath = getRandomTempDbFile();
        String importedDbPath = getRandomTempDbFile();
        String exportFilePath = System.getProperty("java.io.tmpdir")
                + File.separator + "nitrite" + File.separator + "data"
                + File.separator + "nitrite-db-export.json";

        // cleanup
        Path path = Paths.get(initialDbPath);
        Files.deleteIfExists(path);
        Path path1 = Paths.get(exportFilePath);
        Files.deleteIfExists(path1);
        Path path2 = Paths.get(importedDbPath);
        Files.deleteIfExists(path2);

        // create the initial db
        MVStoreModule storeModule = MVStoreModule.withConfig()
                .filePath(initialDbPath)
                .build();

        NitriteId nitriteId;
        try (Nitrite db = Nitrite.builder()
                .loadModule(storeModule)
                .loadModule(new JacksonMapperModule())
                .fieldSeparator(".")
                .openOrCreate()) {

            ObjectRepository<Widget> widgetRepo = db.getRepository(Widget.class);

            // insert a widget
            Widget widget = new Widget();
            widget.setLocalDateEpochDay(LocalDate.now().toEpochDay());
            WriteResult result = widgetRepo.insert(widget);
            nitriteId = result.iterator().next();

            // retrieve the widget as a Document to check the stored type
            NitriteCollection collection = widgetRepo.getDocumentCollection();
            Document widgetDoc = collection.getById(nitriteId);
            Object value = widgetDoc.get("localDateEpochDay");
            assertTrue(value instanceof Long);

            widgetRepo.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "localDateEpochDay");

            // export the db
            Exporter exporter = Exporter.of(db);
            exporter.exportTo(exportFilePath);
        }

        // import the db
        storeModule = MVStoreModule.withConfig()
                .filePath(importedDbPath)
                .build();

        try (Nitrite db = Nitrite.builder()
                .loadModule(storeModule)
                .loadModule(new JacksonMapperModule())
                .fieldSeparator(".")
                .openOrCreate()) {
            Importer importer = Importer.of(db);
            importer.importFrom(exportFilePath);

            // retrieve the widget as a Document to check the stored type
            ObjectRepository<Widget> widgetRepo = db.getRepository(Widget.class);
            NitriteCollection collection = widgetRepo.getDocumentCollection();
            Document widgetDoc = collection.getById(nitriteId);
            Object value = widgetDoc.get("localDateEpochDay");
            assertTrue(value instanceof Long);

            // insert some more widgets
            for (int i = 0; i < 2; i++) {
                Widget widget = new Widget();
                widget.setLocalDateEpochDay(LocalDate.now().toEpochDay());
                widgetRepo.insert(widget);
            }

            // retrieve the widgets
            Cursor<Widget> widgetCursor = widgetRepo.find(FindOptions.orderBy("localDateEpochDay",
                    SortOrder.Descending));
            for (Widget widget : widgetCursor) {
                assertNotNull(widget);
            }
        }

        // cleanup
        Files.deleteIfExists(path);
        Files.deleteIfExists(path1);
        Files.deleteIfExists(path2);
    }

    @Data
    static class Widget {
        @Id
        NitriteId id;
        Long localDateEpochDay;
    }

}
