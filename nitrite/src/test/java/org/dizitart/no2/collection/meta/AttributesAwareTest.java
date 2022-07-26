package org.dizitart.no2.collection.meta;

import org.dizitart.no2.common.meta.Attributes;
import org.dizitart.no2.common.meta.AttributesAware;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertNull;

public class AttributesAwareTest {
    @Test
    public void testGetAttributes() {
        AttributesAware attributesAware = new AttributesAware() {
            @Override
            public Attributes getAttributes() {
                return new Attributes();
            }

            @Override
            public void setAttributes(Attributes attributes) {

            }
        };
        Assert.assertNotNull("This is a boilerplate assert on the result.", attributesAware.getAttributes());
    }

    @Test
    public void testSetAttributes() {
        AttributesAware attributesAware = new AttributesAware() {
            @Override
            public Attributes getAttributes() {
                return null;
            }

            @Override
            public void setAttributes(Attributes attributes) {
                assertNull(attributes);
            }
        };
        attributesAware.setAttributes(null);
    }
}

