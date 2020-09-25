package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class SesothoTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Sesotho()).stopWords();
        assertEquals(31, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("tsa"));
        assertTrue(actualStopWordsResult.contains("ho"));
        assertTrue(actualStopWordsResult.contains("tse"));
        assertTrue(actualStopWordsResult.contains("hore"));
        assertTrue(actualStopWordsResult.contains("moo"));
        assertTrue(actualStopWordsResult.contains("hae"));
        assertTrue(actualStopWordsResult.contains("sa"));
        assertTrue(actualStopWordsResult.contains("oa"));
        assertTrue(actualStopWordsResult.contains("se"));
        assertTrue(actualStopWordsResult.contains("ka"));
        assertTrue(actualStopWordsResult.contains("bane"));
    }
}

