package org.dizitart.no2.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.dizitart.no2.util.Iterables.firstOrDefault;
import static org.dizitart.no2.util.Iterables.toArray;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Anindya Chatterjee.
 */
public class IterablesTest {
    @Test
    public void testFirstOrDefault() {
        assertNull(firstOrDefault(new ArrayList<>()));
        assertNull(firstOrDefault(null));
    }

    @Test
    public void testToArray() {
        final List<String> list = new ArrayList<String>(){{
            add("a");
            add("b");
        }};
        assertArrayEquals(toArray(new ArrayList<String>() {{add("a"); add("b"); }}),
                new String[] {"a", "b"});
        assertArrayEquals(toArray(new Iterable() {
            @Override
            public Iterator iterator() {
                return list.iterator();
            }
        }), new String[] {"a", "b"});
    }
}
