package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class MarathiTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Marathi()).stopWords();
        assertEquals(99, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("होते"));
        assertTrue(actualStopWordsResult.contains("आता"));
        assertTrue(actualStopWordsResult.contains("त्याना"));
        assertTrue(actualStopWordsResult.contains("असून"));
        assertTrue(actualStopWordsResult.contains("पाटील"));
        assertTrue(actualStopWordsResult.contains("तसेच"));
        assertTrue(actualStopWordsResult.contains("येत"));
        assertTrue(actualStopWordsResult.contains("होत"));
        assertTrue(actualStopWordsResult.contains("कोटी"));
        assertTrue(actualStopWordsResult.contains("त्या"));
        assertTrue(actualStopWordsResult.contains("त्याच्या"));
    }
}

