package org.dizitart.no2.index.fulltext.languages;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BengaliTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Bengali()).stopWords();
        assertEquals(398, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("ছিলেন"));
        assertTrue(actualStopWordsResult.contains("প্রায়"));
        assertTrue(actualStopWordsResult.contains("টি"));
        assertTrue(actualStopWordsResult.contains("প্রাথমিক"));
        assertTrue(actualStopWordsResult.contains("করায়"));
        assertTrue(actualStopWordsResult.contains("খুব"));
        assertTrue(actualStopWordsResult.contains("তাই"));
        assertTrue(actualStopWordsResult.contains("সুতরাং"));
        assertTrue(actualStopWordsResult.contains("নেওয়া"));
        assertTrue(actualStopWordsResult.contains("কিংবা"));
        assertTrue(actualStopWordsResult.contains("যেতে"));
    }
}

