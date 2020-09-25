package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class BasqueTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Basque()).stopWords();
        assertEquals(98, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("hi"));
        assertTrue(actualStopWordsResult.contains("horko"));
        assertTrue(actualStopWordsResult.contains("berori"));
        assertTrue(actualStopWordsResult.contains("dago"));
        assertTrue(actualStopWordsResult.contains("bat"));
        assertTrue(actualStopWordsResult.contains("hauetan"));
        assertTrue(actualStopWordsResult.contains("zuek"));
        assertTrue(actualStopWordsResult.contains("berauek"));
        assertTrue(actualStopWordsResult.contains("zuen"));
        assertTrue(actualStopWordsResult.contains("hala"));
        assertTrue(actualStopWordsResult.contains("guzti"));
    }
}

