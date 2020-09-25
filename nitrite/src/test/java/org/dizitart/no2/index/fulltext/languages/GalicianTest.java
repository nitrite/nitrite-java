package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class GalicianTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Galician()).stopWords();
        assertEquals(160, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("aos"));
        assertTrue(actualStopWordsResult.contains("meus"));
        assertTrue(actualStopWordsResult.contains("moi"));
        assertTrue(actualStopWordsResult.contains("del"));
        assertTrue(actualStopWordsResult.contains("dunha"));
        assertTrue(actualStopWordsResult.contains("nin"));
        assertTrue(actualStopWordsResult.contains("isto"));
        assertTrue(actualStopWordsResult.contains("túa"));
        assertTrue(actualStopWordsResult.contains("lles"));
        assertTrue(actualStopWordsResult.contains("ten"));
        assertTrue(actualStopWordsResult.contains("dalgúns"));
    }
}

