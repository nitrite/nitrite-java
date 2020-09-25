package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class LatvianTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Latvian()).stopWords();
        assertEquals(161, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("kļūstam"));
        assertTrue(actualStopWordsResult.contains("pār"));
        assertTrue(actualStopWordsResult.contains("diezin"));
        assertTrue(actualStopWordsResult.contains("taču"));
        assertTrue(actualStopWordsResult.contains("virs"));
        assertTrue(actualStopWordsResult.contains("pret"));
        assertTrue(actualStopWordsResult.contains("taps"));
        assertTrue(actualStopWordsResult.contains("tapt"));
        assertTrue(actualStopWordsResult.contains("tomēr"));
        assertTrue(actualStopWordsResult.contains("kļūstat"));
        assertTrue(actualStopWordsResult.contains("nebūt"));
    }
}

