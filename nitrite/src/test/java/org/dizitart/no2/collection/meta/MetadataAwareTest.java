package org.dizitart.no2.collection.meta;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertNull;

public class MetadataAwareTest {
    @Test
    public void testGetAttributes() {
        MetadataAware metadataAware = new MetadataAware() {
            @Override
            public Attributes getAttributes() {
                return new Attributes();
            }

            @Override
            public void setAttributes(Attributes attributes) {

            }
        };
        Assert.assertNotNull("This is a boilerplate assert on the result.", metadataAware.getAttributes());
    }

    @Test
    public void testSetAttributes() {
        MetadataAware metadataAware = new MetadataAware() {
            @Override
            public Attributes getAttributes() {
                return null;
            }

            @Override
            public void setAttributes(Attributes attributes) {
                assertNull(attributes);
            }
        };
        metadataAware.setAttributes(null);
    }
}

