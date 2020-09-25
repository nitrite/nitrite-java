package org.dizitart.no2.index.fulltext;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class UniversalTextTokenizerTest {
    @Test
    public void testConstructor() {
        Languages[] languagesArray = new Languages[]{};
        new UniversalTextTokenizer(languagesArray);
        assertEquals(0, languagesArray.length);
        assertArrayEquals(new Languages[]{}, languagesArray);
    }

    @Test
    public void testConstructor2() {
        Languages[] languagesArray = new Languages[]{Languages.ALL, Languages.ALL, Languages.ALL};
        new UniversalTextTokenizer(languagesArray);
        assertEquals(3, languagesArray.length);
        assertArrayEquals(new Languages[]{Languages.ALL, Languages.ALL, Languages.ALL}, languagesArray);
    }

    @Test
    public void testConstructor3() {
        assertEquals(Languages.ALL, (new UniversalTextTokenizer()).getLanguage());
    }

    @Test
    public void testGetLanguage() {
        assertEquals(Languages.ALL, (new UniversalTextTokenizer()).getLanguage());
    }

    @Test
    public void testStopWords() {
        Set<String> actualStopWordsResult = (new UniversalTextTokenizer()).stopWords();
        assertEquals(18352, actualStopWordsResult.size());
        assertTrue(actualStopWordsResult.contains("ছিলেন"));
        assertTrue(actualStopWordsResult.contains("þeyi"));
        assertTrue(actualStopWordsResult.contains("者"));
        assertTrue(actualStopWordsResult.contains("而"));
        assertTrue(actualStopWordsResult.contains("pã©ldã¡ul"));
        assertTrue(actualStopWordsResult.contains("որպես"));
        assertTrue(actualStopWordsResult.contains("전자"));
        assertTrue(actualStopWordsResult.contains("tuon"));
        assertTrue(actualStopWordsResult.contains("бивша"));
        assertTrue(actualStopWordsResult.contains("herkes"));
        assertTrue(actualStopWordsResult.contains("একটি"));
    }
}

