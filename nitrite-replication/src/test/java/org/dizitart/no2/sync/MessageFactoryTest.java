package org.dizitart.no2.sync;

import org.dizitart.no2.sync.message.MessageHeader;
import org.dizitart.no2.sync.message.MessageType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MessageFactoryTest {
    @Test
    public void testCreateHeader() {
        MessageHeader actualCreateHeaderResult = (new MessageFactory()).createHeader(MessageType.Error,
            "collectionName", "42", "42", "janedoe", "junit-test");
        assertEquals("42", actualCreateHeaderResult.getOrigin());
        assertEquals("collectionName", actualCreateHeaderResult.getCollection());
        assertEquals("42", actualCreateHeaderResult.getTransactionId());
        assertEquals(MessageType.Error, actualCreateHeaderResult.getMessageType());
        assertEquals("janedoe", actualCreateHeaderResult.getUserName());
        assertEquals("junit-test", actualCreateHeaderResult.getTenant());
    }
}

