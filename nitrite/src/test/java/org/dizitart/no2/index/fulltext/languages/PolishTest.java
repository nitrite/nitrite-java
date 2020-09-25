package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class PolishTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Polish()).stopWords();
        assertEquals(356, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("naszych"));
        assertTrue(actualStopWordsResult.contains("zł"));
        assertTrue(actualStopWordsResult.contains("dzisiaj"));
        assertTrue(actualStopWordsResult.contains("nic"));
        assertTrue(actualStopWordsResult.contains("sobą"));
        assertTrue(actualStopWordsResult.contains("zapewne"));
        assertTrue(actualStopWordsResult.contains("nie"));
        assertTrue(actualStopWordsResult.contains("sposób"));
        assertTrue(actualStopWordsResult.contains("mimo"));
        assertTrue(actualStopWordsResult.contains("jedno"));
        assertTrue(actualStopWordsResult.contains("jemu"));
    }
}

