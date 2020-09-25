package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class IrishTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Irish()).stopWords();
        assertEquals(109, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("aon"));
        assertTrue(actualStopWordsResult.contains("b'"));
        assertTrue(actualStopWordsResult.contains("fara"));
        assertTrue(actualStopWordsResult.contains("tú"));
        assertTrue(actualStopWordsResult.contains("thú"));
        assertTrue(actualStopWordsResult.contains("chtó"));
        assertTrue(actualStopWordsResult.contains("den"));
        assertTrue(actualStopWordsResult.contains("dó"));
        assertTrue(actualStopWordsResult.contains("dár"));
        assertTrue(actualStopWordsResult.contains("seachtar"));
        assertTrue(actualStopWordsResult.contains("seisear"));
    }
}

