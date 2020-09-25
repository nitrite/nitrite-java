package org.dizitart.no2.sync.message;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MessageTypeTest {
    @Test
    public void testCode() {
        assertEquals("no2.sync.error", MessageType.Error.code());
    }
}

