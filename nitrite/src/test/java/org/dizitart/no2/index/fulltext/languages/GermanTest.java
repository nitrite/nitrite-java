package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class GermanTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new German()).stopWords();
        assertEquals(621, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("daß"));
        assertTrue(actualStopWordsResult.contains("jene"));
        assertTrue(actualStopWordsResult.contains("ei,"));
        assertTrue(actualStopWordsResult.contains("sollen"));
        assertTrue(actualStopWordsResult.contains("hattest"));
        assertTrue(actualStopWordsResult.contains("den"));
        assertTrue(actualStopWordsResult.contains("dem"));
        assertTrue(actualStopWordsResult.contains("der"));
        assertTrue(actualStopWordsResult.contains("gesagt"));
        assertTrue(actualStopWordsResult.contains("des"));
        assertTrue(actualStopWordsResult.contains("offen"));
    }
}

