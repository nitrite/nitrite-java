package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class FrenchTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new French()).stopWords();
        assertEquals(689, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("certaine"));
        assertTrue(actualStopWordsResult.contains("lui"));
        assertTrue(actualStopWordsResult.contains("quelques"));
        assertTrue(actualStopWordsResult.contains("trois"));
        assertTrue(actualStopWordsResult.contains("aucun"));
        assertTrue(actualStopWordsResult.contains("mince"));
        assertTrue(actualStopWordsResult.contains("ouvert"));
        assertTrue(actualStopWordsResult.contains("egale"));
        assertTrue(actualStopWordsResult.contains("bah"));
        assertTrue(actualStopWordsResult.contains("notamment"));
        assertTrue(actualStopWordsResult.contains("quanta"));
    }
}

