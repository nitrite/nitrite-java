package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class EsperantoTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Esperanto()).stopWords();
        assertEquals(173, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("malgraŭ"));
        assertTrue(actualStopWordsResult.contains("onia"));
        assertTrue(actualStopWordsResult.contains("ĝis"));
        assertTrue(actualStopWordsResult.contains("oka"));
        assertTrue(actualStopWordsResult.contains("ĝin"));
        assertTrue(actualStopWordsResult.contains("ho"));
        assertTrue(actualStopWordsResult.contains("nia"));
        assertTrue(actualStopWordsResult.contains("tuj"));
        assertTrue(actualStopWordsResult.contains("ion"));
        assertTrue(actualStopWordsResult.contains("malantaŭ"));
        assertTrue(actualStopWordsResult.contains("preter"));
    }
}

