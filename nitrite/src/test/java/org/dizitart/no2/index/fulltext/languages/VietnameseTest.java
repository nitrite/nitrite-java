package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class VietnameseTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Vietnamese()).stopWords();
        assertEquals(645, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("quá độ"));
        assertTrue(actualStopWordsResult.contains("tất cả"));
        assertTrue(actualStopWordsResult.contains("bằng nấy"));
        assertTrue(actualStopWordsResult.contains("tò te"));
        assertTrue(actualStopWordsResult.contains("do đó"));
        assertTrue(actualStopWordsResult.contains("chung quy"));
        assertTrue(actualStopWordsResult.contains("ô hay"));
        assertTrue(actualStopWordsResult.contains("ô kìa"));
        assertTrue(actualStopWordsResult.contains("ối giời ơi"));
        assertTrue(actualStopWordsResult.contains("ai nấy"));
        assertTrue(actualStopWordsResult.contains("cùng với"));
    }
}

