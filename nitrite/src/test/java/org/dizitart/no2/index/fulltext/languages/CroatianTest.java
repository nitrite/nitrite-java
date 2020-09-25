package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class CroatianTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Croatian()).stopWords();
        assertEquals(179, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("bih"));
        assertTrue(actualStopWordsResult.contains("jedne"));
        assertTrue(actualStopWordsResult.contains("nije"));
        assertTrue(actualStopWordsResult.contains("neće"));
        assertTrue(actualStopWordsResult.contains("kako"));
        assertTrue(actualStopWordsResult.contains("vrlo"));
        assertTrue(actualStopWordsResult.contains("bio"));
        assertTrue(actualStopWordsResult.contains("moj"));
        assertTrue(actualStopWordsResult.contains("duž"));
        assertTrue(actualStopWordsResult.contains("jeste"));
        assertTrue(actualStopWordsResult.contains("mimo"));
    }
}

