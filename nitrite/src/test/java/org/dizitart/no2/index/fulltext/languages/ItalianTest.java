package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class ItalianTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Italian()).stopWords();
        assertEquals(660, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("fare"));
        assertTrue(actualStopWordsResult.contains("lui"));
        assertTrue(actualStopWordsResult.contains("triplo"));
        assertTrue(actualStopWordsResult.contains("nove"));
        assertTrue(actualStopWordsResult.contains("governo"));
        assertTrue(actualStopWordsResult.contains("avevo"));
        assertTrue(actualStopWordsResult.contains("avranno"));
        assertTrue(actualStopWordsResult.contains("chiunque"));
        assertTrue(actualStopWordsResult.contains("dei"));
        assertTrue(actualStopWordsResult.contains("sugli"));
        assertTrue(actualStopWordsResult.contains("del"));
    }
}

