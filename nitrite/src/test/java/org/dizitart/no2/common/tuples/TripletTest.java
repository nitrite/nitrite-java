package org.dizitart.no2.common.tuples;

import org.junit.Test;

import static org.junit.Assert.*;

public class TripletTest {
    @Test
    public void testCanEqual() {
        assertFalse((new Triplet<>()).canEqual("other"));
    }

    @Test
    public void testEquals() {
        Triplet triplet = new Triplet(new Triplet(), "second", "third");
        assertFalse(triplet.equals(new Triplet()));
    }

    @Test
    public void testEquals10() {
        Triplet triplet = new Triplet("first", "second", new Triplet());
        assertFalse(triplet.equals(new Triplet("first", "second", "third")));
    }

    @Test
    public void testEquals11() {
        Triplet triplet = new Triplet("first", "second", "third");
        assertFalse(triplet.equals(new Triplet()));
    }

    @Test
    public void testEquals2() {
        Triplet o = new Triplet("first", "second", "third");
        assertFalse((new Triplet<>()).equals(o));
    }

    @Test
    public void testEquals3() {
        Triplet o = new Triplet(null, "second", "third");
        assertFalse((new Triplet<>()).equals(o));
    }

    @Test
    public void testEquals4() {
        Triplet triplet = new Triplet("first", "second", "third");
        assertTrue(triplet.equals(new Triplet("first", "second", "third")));
    }

    @Test
    public void testEquals5() {
        assertFalse((new Triplet<>()).equals("o"));
    }

    @Test
    public void testEquals6() {
        Triplet<Object, Object, Object> triplet = new Triplet<>();
        assertTrue(triplet.equals(new Triplet()));
    }

    @Test
    public void testEquals7() {
        Triplet triplet = new Triplet("first", new Triplet(), "third");
        assertFalse(triplet.equals(new Triplet("first", "second", "third")));
    }

    @Test
    public void testEquals8() {
        Triplet triplet = new Triplet("first", "second", null);
        assertFalse(triplet.equals(new Triplet("first", "second", "third")));
    }

    @Test
    public void testEquals9() {
        Triplet triplet = new Triplet(null, "second", "third");
        assertFalse(triplet.equals(new Triplet()));
    }

    @Test
    public void testSetFirst() {
        Triplet<Object, Object, Object> triplet = new Triplet<>();
        triplet.setFirst("first");
        assertEquals("Triplet(first=first, second=null, third=null)", triplet.toString());
    }

    @Test
    public void testSetSecond() {
        Triplet<Object, Object, Object> triplet = new Triplet<>();
        triplet.setSecond("second");
        assertEquals("Triplet(first=null, second=second, third=null)", triplet.toString());
    }

    @Test
    public void testSetThird() {
        Triplet<Object, Object, Object> triplet = new Triplet<>();
        triplet.setThird("third");
        assertEquals("Triplet(first=null, second=null, third=third)", triplet.toString());
    }

    @Test
    public void testToString() {
        assertEquals("Triplet(first=null, second=null, third=null)", (new Triplet<>()).toString());
    }
}

