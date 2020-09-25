package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class HungarianTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Hungarian()).stopWords();
        assertEquals(1185, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("mögüled"));
        assertTrue(actualStopWordsResult.contains("viszont"));
        assertTrue(actualStopWordsResult.contains("azokon"));
        assertTrue(actualStopWordsResult.contains("korábban"));
        assertTrue(actualStopWordsResult.contains("távol"));
        assertTrue(actualStopWordsResult.contains("ã¡m"));
        assertTrue(actualStopWordsResult.contains("elől"));
        assertTrue(actualStopWordsResult.contains("ezer"));
        assertTrue(actualStopWordsResult.contains("pã©ldã¡ul"));
        assertTrue(actualStopWordsResult.contains("rã¡"));
        assertTrue(actualStopWordsResult.contains("rã¡tok"));
    }
}

