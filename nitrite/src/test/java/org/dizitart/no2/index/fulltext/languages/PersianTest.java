package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class PersianTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Persian()).stopWords();
        assertEquals(799, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("برخوردار"));
        assertTrue(actualStopWordsResult.contains("بسياري"));
        assertTrue(actualStopWordsResult.contains("ناشي"));
        assertTrue(actualStopWordsResult.contains("یابد"));
        assertTrue(actualStopWordsResult.contains("داریم"));
        assertTrue(actualStopWordsResult.contains("یابم"));
        assertTrue(actualStopWordsResult.contains("يابد"));
        assertTrue(actualStopWordsResult.contains("هنگامی"));
        assertTrue(actualStopWordsResult.contains("کمتر"));
        assertTrue(actualStopWordsResult.contains("بيست"));
        assertTrue(actualStopWordsResult.contains("تمامي"));
    }
}

