package org.dizitart.no2;

import org.dizitart.no2.event.ChangeType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class ChangeTypeTest {

    @Test
    public void testValueOf() {
        assertEquals(ChangeType.valueOf("INSERT"), ChangeType.INSERT);
        assertEquals(ChangeType.valueOf("CLOSE"), ChangeType.CLOSE);
        assertEquals(ChangeType.valueOf("DROP"), ChangeType.DROP);
        assertEquals(ChangeType.valueOf("UPDATE"), ChangeType.UPDATE);
        assertEquals(ChangeType.valueOf("REMOVE"), ChangeType.REMOVE);

        assertEquals(ChangeType.values().length, 5);
    }
}
