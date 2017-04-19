package org.dizitart.no2.util;

import org.junit.Test;

import static org.dizitart.no2.util.StringUtils.isNullOrEmpty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee.
 */
public class StringUtilsTest {

    @Test
    public void testIsNullOrEmpty() {
        assertTrue(isNullOrEmpty(null));
        assertTrue(isNullOrEmpty(""));
        assertFalse(isNullOrEmpty("a"));
    }
}
