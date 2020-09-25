package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class SwahiliTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Swahili()).stopWords();
        assertEquals(74, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("hivyo"));
        assertTrue(actualStopWordsResult.contains("alisema"));
        assertTrue(actualStopWordsResult.contains("kwenda"));
        assertTrue(actualStopWordsResult.contains("tu"));
        assertTrue(actualStopWordsResult.contains("akasema"));
        assertTrue(actualStopWordsResult.contains("karibu"));
        assertTrue(actualStopWordsResult.contains("yangu"));
        assertTrue(actualStopWordsResult.contains("huo"));
        assertTrue(actualStopWordsResult.contains("nini"));
        assertTrue(actualStopWordsResult.contains("huku"));
        assertTrue(actualStopWordsResult.contains("kuwa"));
    }
}

