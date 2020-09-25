package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class SpanishTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Spanish()).stopWords();
        assertEquals(732, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("bastante"));
        assertTrue(actualStopWordsResult.contains("hubiera"));
        assertTrue(actualStopWordsResult.contains("pronto"));
        assertTrue(actualStopWordsResult.contains("mismo"));
        assertTrue(actualStopWordsResult.contains("del"));
        assertTrue(actualStopWordsResult.contains("fin"));
        assertTrue(actualStopWordsResult.contains("esté"));
        assertTrue(actualStopWordsResult.contains("hubiesen"));
        assertTrue(actualStopWordsResult.contains("toda"));
        assertTrue(actualStopWordsResult.contains("posible"));
        assertTrue(actualStopWordsResult.contains("buen"));
    }
}

