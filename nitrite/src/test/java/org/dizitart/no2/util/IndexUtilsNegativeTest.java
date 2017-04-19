package org.dizitart.no2.util;

import org.dizitart.no2.Index;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.exceptions.ValidationException;
import org.junit.Test;

import static org.dizitart.no2.util.IndexUtils.internalName;

/**
 * @author Anindya Chatterjee.
 */
public class IndexUtilsNegativeTest {
    @Test(expected = ValidationException.class)
    public void testInternalNameInvalidIndex() {
        Index idx = new Index(IndexType.NonUnique, null, "");
        internalName(idx);
    }
}
