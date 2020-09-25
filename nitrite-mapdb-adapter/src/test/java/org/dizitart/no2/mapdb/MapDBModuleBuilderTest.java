package org.dizitart.no2.mapdb;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class MapDBModuleBuilderTest {
    @Test
    public void testConstructor() {
        MapDBModuleBuilder actualMapDBModuleBuilder = new MapDBModuleBuilder();
        assertNull(actualMapDBModuleBuilder.isThreadSafe());
        assertNull(actualMapDBModuleBuilder.volume());
        assertFalse(actualMapDBModuleBuilder.readOnly());
    }
}

