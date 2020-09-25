package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class MalayTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Malay()).stopWords();
        assertEquals(475, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("sejak"));
        assertTrue(actualStopWordsResult.contains("amerika"));
        assertTrue(actualStopWordsResult.contains("perjanjian"));
        assertTrue(actualStopWordsResult.contains("mempunyai"));
        assertTrue(actualStopWordsResult.contains("tenaga"));
        assertTrue(actualStopWordsResult.contains("utama"));
        assertTrue(actualStopWordsResult.contains("seorang"));
        assertTrue(actualStopWordsResult.contains("daripada"));
        assertTrue(actualStopWordsResult.contains("mencatatkan"));
        assertTrue(actualStopWordsResult.contains("pegawai"));
        assertTrue(actualStopWordsResult.contains("sesi"));
    }
}

