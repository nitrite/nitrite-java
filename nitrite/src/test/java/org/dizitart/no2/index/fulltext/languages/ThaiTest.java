package org.dizitart.no2.index.fulltext.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class ThaiTest {
    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new Thai()).stopWords();
        assertEquals(116, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("ด้าน"));
        assertTrue(actualStopWordsResult.contains("ตั้ง"));
        assertTrue(actualStopWordsResult.contains("แรก"));
        assertTrue(actualStopWordsResult.contains("รวม"));
        assertTrue(actualStopWordsResult.contains("เปิดเผย"));
        assertTrue(actualStopWordsResult.contains("ถึง"));
        assertTrue(actualStopWordsResult.contains("ซึ่ง"));
        assertTrue(actualStopWordsResult.contains("เฉพาะ"));
        assertTrue(actualStopWordsResult.contains("ทุก"));
        assertTrue(actualStopWordsResult.contains("ส่วน"));
        assertTrue(actualStopWordsResult.contains("จาก"));
    }
}

