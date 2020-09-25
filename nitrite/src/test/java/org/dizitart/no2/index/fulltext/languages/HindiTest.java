package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class HindiTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Hindi()).stopWords();
        assertEquals(225, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("जितना"));
        assertTrue(actualStopWordsResult.contains("द्वारा"));
        assertTrue(actualStopWordsResult.contains("एस"));
        assertTrue(actualStopWordsResult.contains("तरह"));
        assertTrue(actualStopWordsResult.contains("इसमें"));
        assertTrue(actualStopWordsResult.contains("सकते"));
        assertTrue(actualStopWordsResult.contains("अंदर"));
        assertTrue(actualStopWordsResult.contains("जिंहों"));
        assertTrue(actualStopWordsResult.contains("फिर"));
        assertTrue(actualStopWordsResult.contains("बहुत"));
        assertTrue(actualStopWordsResult.contains("कहते"));
    }
}

