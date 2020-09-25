package org.dizitart.no2.index.fulltext.languages;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UrduTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Urdu()).stopWords();
        assertEquals(517, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("توہیں"));
        assertTrue(actualStopWordsResult.contains("ڈھوًڈا"));
        assertTrue(actualStopWordsResult.contains("ضروری"));
        assertTrue(actualStopWordsResult.contains("فرد"));
        assertTrue(actualStopWordsResult.contains("دلچطپیبں"));
        assertTrue(actualStopWordsResult.contains("چھوٹب"));
        assertTrue(actualStopWordsResult.contains("کہو"));
        assertTrue(actualStopWordsResult.contains("کورا"));
        assertTrue(actualStopWordsResult.contains("دلچطپ"));
        assertTrue(actualStopWordsResult.contains("ہوًب"));
        assertTrue(actualStopWordsResult.contains("ہوتب"));
    }
}

