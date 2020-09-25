package org.dizitart.no2.sync.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Test;

public class ReceiptTest {
    @Test
    public void testCanEqual() {
        assertFalse((new Receipt()).canEqual("other"));
    }

    @Test
    public void testConstructor() {
        HashSet<String> added = new HashSet<String>();
        assertEquals("Receipt(added=[], removed=[])", (new Receipt(added, new HashSet<String>())).toString());
    }

    @Test
    public void testConstructor2() {
        assertEquals("Receipt(added=[], removed=[])", (new Receipt()).toString());
    }

    @Test
    public void testEquals() {
        Receipt receipt = new Receipt();
        receipt.setRemoved(null);
        assertFalse(receipt.equals(new Receipt()));
    }

    @Test
    public void testEquals2() {
        assertFalse((new Receipt()).equals("o"));
    }

    @Test
    public void testEquals3() {
        Receipt receipt = new Receipt();
        receipt.setAdded(null);
        assertFalse(receipt.equals(new Receipt()));
    }

    @Test
    public void testEquals4() {
        Receipt receipt = new Receipt();
        receipt.setRemoved(null);
        assertFalse((new Receipt()).equals(receipt));
    }

    @Test
    public void testEquals5() {
        Receipt receipt = new Receipt();
        assertTrue(receipt.equals(new Receipt()));
    }

    @Test
    public void testEquals6() {
        Receipt receipt = new Receipt();
        receipt.setAdded(null);
        assertFalse((new Receipt()).equals(receipt));
    }

    @Test
    public void testHashCode() {
        Receipt receipt = new Receipt();
        receipt.setAdded(null);
        assertEquals(6018, receipt.hashCode());
    }

    @Test
    public void testHashCode2() {
        assertEquals(3481, (new Receipt()).hashCode());
    }

    @Test
    public void testHashCode3() {
        Receipt receipt = new Receipt();
        receipt.setRemoved(null);
        assertEquals(3524, receipt.hashCode());
    }

    @Test
    public void testSetAdded() {
        Receipt receipt = new Receipt();
        receipt.setAdded(new HashSet<String>());
        assertEquals("Receipt(added=[], removed=[])", receipt.toString());
    }

    @Test
    public void testSetRemoved() {
        Receipt receipt = new Receipt();
        receipt.setRemoved(new HashSet<String>());
        assertEquals("Receipt(added=[], removed=[])", receipt.toString());
    }

    @Test
    public void testToString() {
        assertEquals("Receipt(added=[], removed=[])", (new Receipt()).toString());
    }
}

