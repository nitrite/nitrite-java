package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class BretonTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Breton()).stopWords();
        assertEquals(1203, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("fezh"));
        assertTrue(actualStopWordsResult.contains("c'he"));
        assertTrue(actualStopWordsResult.contains("eilvet"));
        assertTrue(actualStopWordsResult.contains("peurgetket"));
        assertTrue(actualStopWordsResult.contains("goude"));
        assertTrue(actualStopWordsResult.contains("warno"));
        assertTrue(actualStopWordsResult.contains("zoken"));
        assertTrue(actualStopWordsResult.contains("rae"));
        assertTrue(actualStopWordsResult.contains("rag"));
        assertTrue(actualStopWordsResult.contains("warni"));
        assertTrue(actualStopWordsResult.contains("bepred"));
    }
}

