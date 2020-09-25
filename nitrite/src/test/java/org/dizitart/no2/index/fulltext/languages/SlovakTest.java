package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class SlovakTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Slovak()).stopWords();
        assertEquals(221, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("toto"));
        assertTrue(actualStopWordsResult.contains("preto"));
        assertTrue(actualStopWordsResult.contains("niektorý"));
        assertTrue(actualStopWordsResult.contains("nie"));
        assertTrue(actualStopWordsResult.contains("ním"));
        assertTrue(actualStopWordsResult.contains("všetok"));
        assertTrue(actualStopWordsResult.contains("táto"));
        assertTrue(actualStopWordsResult.contains("aký"));
        assertTrue(actualStopWordsResult.contains("jemu"));
        assertTrue(actualStopWordsResult.contains("nim"));
        assertTrue(actualStopWordsResult.contains("svojími"));
    }
}

