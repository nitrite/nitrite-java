package org.dizitart.no2.collection;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UpdateOptionsTest {
    @Test
    public void testUpdateOptions() {
        UpdateOptions actualUpdateOptionsResult = UpdateOptions.updateOptions(true, true);
        assertTrue(actualUpdateOptionsResult.isInsertIfAbsent());
        assertTrue(actualUpdateOptionsResult.isJustOnce());
    }

    @Test
    public void testUpdateOptions2() {
        assertTrue(UpdateOptions.updateOptions(true).isInsertIfAbsent());
    }
}

