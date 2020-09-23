package org.dizitart.no2.store.memory;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class InMemoryRTreeTest {
    @Test
    public void testConstructor() {
        assertEquals(0L, (new InMemoryRTree<>()).size());
    }

    @Test
    public void testSize() {
        assertEquals(0L, (new InMemoryRTree<>()).size());
    }

    @Test
    public void testSpatialKeyConstructor() {
        float[] floatArray = new float[]{10.0f, 10.0f, 10.0f, 10.0f, 10.0f, 10.0f, 10.0f, 10.0f};
        new InMemoryRTree.SpatialKey(123L, floatArray);
        assertEquals(8, floatArray.length);
        assertArrayEquals(new float[]{10.0f, 10.0f, 10.0f, 10.0f, 10.0f, 10.0f, 10.0f, 10.0f}, floatArray, 0.0f);
    }
}

