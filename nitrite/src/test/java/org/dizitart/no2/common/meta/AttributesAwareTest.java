/*
 * Copyright (c) 2017-2022 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.common.meta;

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

