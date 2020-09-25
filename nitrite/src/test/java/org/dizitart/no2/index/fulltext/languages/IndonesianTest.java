package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class IndonesianTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Indonesian()).stopWords();
        assertEquals(758, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("diakhirinya"));
        assertTrue(actualStopWordsResult.contains("dimulailah"));
        assertTrue(actualStopWordsResult.contains("ibarat"));
        assertTrue(actualStopWordsResult.contains("mengucapkannya"));
        assertTrue(actualStopWordsResult.contains("mengucapkan"));
        assertTrue(actualStopWordsResult.contains("tertuju"));
        assertTrue(actualStopWordsResult.contains("sejak"));
        assertTrue(actualStopWordsResult.contains("mempunyai"));
        assertTrue(actualStopWordsResult.contains("sebisanya"));
        assertTrue(actualStopWordsResult.contains("menyampaikan"));
        assertTrue(actualStopWordsResult.contains("menanyai"));
    }
}

