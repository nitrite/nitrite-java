package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class RussianTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Russian()).stopWords();
        assertEquals(559, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("девять"));
        assertTrue(actualStopWordsResult.contains("новый"));
        assertTrue(actualStopWordsResult.contains("день"));
        assertTrue(actualStopWordsResult.contains("занят"));
        assertTrue(actualStopWordsResult.contains("этому"));
        assertTrue(actualStopWordsResult.contains("ещё"));
        assertTrue(actualStopWordsResult.contains("найти"));
        assertTrue(actualStopWordsResult.contains("тобой"));
        assertTrue(actualStopWordsResult.contains("еще"));
        assertTrue(actualStopWordsResult.contains("ним"));
        assertTrue(actualStopWordsResult.contains("спасибо"));
    }
}

