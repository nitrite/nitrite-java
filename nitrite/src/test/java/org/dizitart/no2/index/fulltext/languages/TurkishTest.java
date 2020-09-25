package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class TurkishTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Turkish()).stopWords();
        assertEquals(504, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("þeyi"));
        assertTrue(actualStopWordsResult.contains("senin"));
        assertTrue(actualStopWordsResult.contains("hiç"));
        assertTrue(actualStopWordsResult.contains("þundan"));
        assertTrue(actualStopWordsResult.contains("birileri"));
        assertTrue(actualStopWordsResult.contains("yapılması"));
        assertTrue(actualStopWordsResult.contains("dek"));
        assertTrue(actualStopWordsResult.contains("geçenlerde"));
        assertTrue(actualStopWordsResult.contains("hariç"));
        assertTrue(actualStopWordsResult.contains("böylemesine"));
        assertTrue(actualStopWordsResult.contains("olmak"));
    }
}

