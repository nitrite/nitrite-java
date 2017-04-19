package org.dizitart.no2;

import org.dizitart.no2.fulltext.EnglishTextTokenizer;
import org.dizitart.no2.fulltext.TextIndexingService;
import org.dizitart.no2.fulltext.TextTokenizer;
import org.dizitart.no2.services.LuceneService;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.dizitart.no2.util.StringUtils.isNullOrEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee.
 */
public class NitriteBuilderTest {

    @Test
    public void testConfig() throws IOException {
        TextIndexingService textIndexingService = new LuceneService();
        TextTokenizer textTokenizer = new EnglishTextTokenizer();
        String filePath = getRandomTempDbFile();

        NitriteBuilder builder = Nitrite.builder();
        builder.autoCommitBufferSize(1);
        builder.compressed();
        builder.disableAutoCommit();
        builder.disableAutoCompact();
        builder.filePath(filePath);
        builder.textIndexingService(textIndexingService);
        builder.textTokenizer(textTokenizer);

        Nitrite db = builder.openOrCreate();
        NitriteContext context = db.getContext();

        assertEquals(context.getAutoCommitBufferSize(), 1);
        assertEquals(context.getTextIndexingService(), textIndexingService);
        assertEquals(context.getTextTokenizer(), textTokenizer);
        assertFalse(context.isAutoCommitEnabled());
        assertFalse(context.isAutoCompactEnabled());
        assertTrue(context.isCompressed());
        assertFalse(context.isReadOnly());
        assertFalse(context.isInMemory());
        assertFalse(isNullOrEmpty(context.getFilePath()));

        db.close();

        db = Nitrite.builder()
                .readOnly()
                .filePath(filePath)
                .openOrCreate();
        context = db.getContext();
        assertTrue(context.isReadOnly());
        db.close();

        Files.delete(Paths.get(filePath));
    }
}
