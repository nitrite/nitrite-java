package org.dizitart.no2.sync;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ReplicationExceptionTest {
    @Test
    public void testConstructor() {
        assertTrue((new ReplicationException("An error occurred", true)).isFatal());
        assertTrue((new ReplicationException("An error occurred", new Throwable(), true)).isFatal());
    }
}

