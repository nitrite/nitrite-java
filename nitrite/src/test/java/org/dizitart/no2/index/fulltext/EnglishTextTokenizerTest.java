package org.dizitart.no2.index.fulltext;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EnglishTextTokenizerTest {
    @Test
    public void testConstructor() {
        assertEquals(Languages.English, (new EnglishTextTokenizer()).getLanguage());
    }

    @Test
    public void testGetLanguage() {
        assertEquals(Languages.English, (new EnglishTextTokenizer()).getLanguage());
    }

    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new EnglishTextTokenizer()).stopWords();
        assertEquals(570, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("sometime"));
        assertTrue(actualStopWordsResult.contains("been"));
        assertTrue(actualStopWordsResult.contains("mostly"));
        assertTrue(actualStopWordsResult.contains("don't"));
        assertTrue(actualStopWordsResult.contains("couldn't"));
        assertTrue(actualStopWordsResult.contains("your"));
        assertTrue(actualStopWordsResult.contains("without"));
        assertTrue(actualStopWordsResult.contains("via"));
        assertTrue(actualStopWordsResult.contains("these"));
        assertTrue(actualStopWordsResult.contains("appreciate"));
        assertTrue(actualStopWordsResult.contains("would"));
    }
}

