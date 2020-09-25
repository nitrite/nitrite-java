package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class TagalogTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Tagalog()).stopWords();
        assertEquals(147, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("pumupunta"));
        assertTrue(actualStopWordsResult.contains("marapat"));
        assertTrue(actualStopWordsResult.contains("mga"));
        assertTrue(actualStopWordsResult.contains("paano"));
        assertTrue(actualStopWordsResult.contains("mismo"));
        assertTrue(actualStopWordsResult.contains("ibabaw"));
        assertTrue(actualStopWordsResult.contains("marami"));
        assertTrue(actualStopWordsResult.contains("muli"));
        assertTrue(actualStopWordsResult.contains("panahon"));
        assertTrue(actualStopWordsResult.contains("lahat"));
        assertTrue(actualStopWordsResult.contains("kailangan"));
    }
}

