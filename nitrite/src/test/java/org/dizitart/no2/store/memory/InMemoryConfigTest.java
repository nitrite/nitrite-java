package org.dizitart.no2.store.memory;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class InMemoryConfigTest {
    @Test
    public void testFilePath() {
        assertNull((new InMemoryConfig()).filePath());
    }

    @Test
    public void testIsReadOnly() {
        assertFalse((new InMemoryConfig()).isReadOnly());
    }
}

