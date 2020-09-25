package org.dizitart.no2.index.fulltext.languages;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ZuluTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Zulu()).stopWords();
        assertEquals(29, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("zakhe"));
        assertTrue(actualStopWordsResult.contains("uma"));
        assertTrue(actualStopWordsResult.contains("kusho"));
        assertTrue(actualStopWordsResult.contains("phezulu"));
        assertTrue(actualStopWordsResult.contains("lakhe"));
        assertTrue(actualStopWordsResult.contains("wami"));
        assertTrue(actualStopWordsResult.contains("wathi"));
        assertTrue(actualStopWordsResult.contains("futhi"));
        assertTrue(actualStopWordsResult.contains("ngesikhathi"));
        assertTrue(actualStopWordsResult.contains("zonke"));
        assertTrue(actualStopWordsResult.contains("kahle"));
    }
}

