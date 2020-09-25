package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class FinnishTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Finnish()).stopWords();
        assertEquals(847, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("mitkä"));
        assertTrue(actualStopWordsResult.contains("vuosina"));
        assertTrue(actualStopWordsResult.contains("heti"));
        assertTrue(actualStopWordsResult.contains("sataa"));
        assertTrue(actualStopWordsResult.contains("pienestä"));
        assertTrue(actualStopWordsResult.contains("muiden"));
        assertTrue(actualStopWordsResult.contains("ympäri"));
        assertTrue(actualStopWordsResult.contains("ainoa"));
        assertTrue(actualStopWordsResult.contains("kenet"));
        assertTrue(actualStopWordsResult.contains("aloittamatta"));
        assertTrue(actualStopWordsResult.contains("hyviä"));
    }
}

