package org.dizitart.no2.common.tuples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class QuintetTest {
    @Test
    public void testCanEqual() {
        assertFalse((new Quintet<Object, Object, Object, Object, Object>()).canEqual("other"));
    }

    @Test
    public void testEquals() {
        Quintet quintet = new Quintet(null, "second", "third", "fourth", "fifth");
        assertFalse(quintet.equals(new Quintet()));
    }

    @Test
    public void testEquals10() {
        Quintet o = new Quintet("first", "second", "third", "fourth", "fifth");
        assertFalse((new Quintet<Object, Object, Object, Object, Object>()).equals(o));
    }

    @Test
    public void testEquals11() {
        Quintet quintet = new Quintet("first", "second", "third", new Quintet(), "fifth");
        assertFalse(quintet.equals(new Quintet("first", "second", "third", "fourth", "fifth")));
    }

    @Test
    public void testEquals12() {
        Quintet quintet = new Quintet("first", "second", "third", "fourth", new Quintet());
        assertFalse(quintet.equals(new Quintet("first", "second", "third", "fourth", "fifth")));
    }

    @Test
    public void testEquals13() {
        Quintet quintet = new Quintet("first", new Quintet(), "third", "fourth", "fifth");
        assertFalse(quintet.equals(new Quintet("first", "second", "third", "fourth", "fifth")));
    }

    @Test
    public void testEquals14() {
        Quintet quintet = new Quintet("first", "second", new Quintet(), "fourth", "fifth");
        assertFalse(quintet.equals(new Quintet("first", "second", "third", "fourth", "fifth")));
    }

    @Test
    public void testEquals15() {
        Quintet quintet = new Quintet("first", "second", "third", "fourth", null);
        assertFalse(quintet.equals(new Quintet("first", "second", "third", "fourth", "fifth")));
    }

    @Test
    public void testEquals2() {
        assertFalse((new Quintet<Object, Object, Object, Object, Object>()).equals("o"));
    }

    @Test
    public void testEquals3() {
        Quintet o = new Quintet(null, "second", "third", "fourth", "fifth");
        assertFalse((new Quintet<Object, Object, Object, Object, Object>()).equals(o));
    }

    @Test
    public void testEquals4() {
        Quintet quintet = new Quintet("first", "second", "third", "fourth", "fifth");
        assertFalse(quintet.equals(new Quintet()));
    }

    @Test
    public void testEquals5() {
        Quintet quintet = new Quintet("first", "second", null, "fourth", "fifth");
        assertFalse(quintet.equals(new Quintet("first", "second", "third", "fourth", "fifth")));
    }

    @Test
    public void testEquals6() {
        Quintet<Object, Object, Object, Object, Object> quintet = new Quintet<Object, Object, Object, Object, Object>();
        assertTrue(quintet.equals(new Quintet()));
    }

    @Test
    public void testEquals7() {
        Quintet quintet = new Quintet("first", "second", "third", null, "fifth");
        assertFalse(quintet.equals(new Quintet("first", "second", "third", "fourth", "fifth")));
    }

    @Test
    public void testEquals8() {
        Quintet quintet = new Quintet("first", "second", "third", "fourth", "fifth");
        assertTrue(quintet.equals(new Quintet("first", "second", "third", "fourth", "fifth")));
    }

    @Test
    public void testEquals9() {
        Quintet quintet = new Quintet(new Quintet(), "second", "third", "fourth", "fifth");
        assertFalse(quintet.equals(new Quintet()));
    }

    @Test
    public void testHashCode() {
        assertEquals(961065563,
            (new Quintet<Object, Object, Object, Object, Object>("first", "second", "third", "fourth", "fifth"))
                .hashCode());
        assertEquals(1244954382, (new Quintet<Object, Object, Object, Object, Object>()).hashCode());
    }

    @Test
    public void testSetFifth() {
        Quintet<Object, Object, Object, Object, Object> quintet = new Quintet<Object, Object, Object, Object, Object>();
        quintet.setFifth("fifth");
        assertEquals("Quintet(first=null, second=null, third=null, fourth=null, fifth=fifth)", quintet.toString());
    }

    @Test
    public void testSetFirst() {
        Quintet<Object, Object, Object, Object, Object> quintet = new Quintet<Object, Object, Object, Object, Object>();
        quintet.setFirst("first");
        assertEquals("Quintet(first=first, second=null, third=null, fourth=null, fifth=null)", quintet.toString());
    }

    @Test
    public void testSetFourth() {
        Quintet<Object, Object, Object, Object, Object> quintet = new Quintet<Object, Object, Object, Object, Object>();
        quintet.setFourth("fourth");
        assertEquals("Quintet(first=null, second=null, third=null, fourth=fourth, fifth=null)", quintet.toString());
    }

    @Test
    public void testSetSecond() {
        Quintet<Object, Object, Object, Object, Object> quintet = new Quintet<Object, Object, Object, Object, Object>();
        quintet.setSecond("second");
        assertEquals("Quintet(first=null, second=second, third=null, fourth=null, fifth=null)", quintet.toString());
    }

    @Test
    public void testSetThird() {
        Quintet<Object, Object, Object, Object, Object> quintet = new Quintet<Object, Object, Object, Object, Object>();
        quintet.setThird("third");
        assertEquals("Quintet(first=null, second=null, third=third, fourth=null, fifth=null)", quintet.toString());
    }

    @Test
    public void testToString() {
        assertEquals("Quintet(first=null, second=null, third=null, fourth=null, fifth=null)",
            (new Quintet<Object, Object, Object, Object, Object>()).toString());
    }
}

