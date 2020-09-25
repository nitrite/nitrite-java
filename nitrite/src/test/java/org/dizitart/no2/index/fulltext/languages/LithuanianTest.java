package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class LithuanianTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Lithuanian()).stopWords();
        assertEquals(474, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("jums"));
        assertTrue(actualStopWordsResult.contains("tavuoju"));
        assertTrue(actualStopWordsResult.contains("anajam"));
        assertTrue(actualStopWordsResult.contains("pats"));
        assertTrue(actualStopWordsResult.contains("ðiaisiais"));
        assertTrue(actualStopWordsResult.contains("nebe"));
        assertTrue(actualStopWordsResult.contains("mudu"));
        assertTrue(actualStopWordsResult.contains("abiejuose"));
        assertTrue(actualStopWordsResult.contains("jiedu"));
        assertTrue(actualStopWordsResult.contains("pati"));
        assertTrue(actualStopWordsResult.contains("vëlgi"));
    }
}

