/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.collection;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.exceptions.ValidationException;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author Anindya Chatterjee
 */
public class CollectionFactoryTest {

    @Test(expected = ValidationException.class)
    public void testGetCollectionMapStoreNull() {
        CollectionFactory factory = new CollectionFactory();
        assertNotNull(factory);

        NitriteConfig config = NitriteConfig.create();
        factory.getCollection(null, config, true);
    }

    @Test(expected = ValidationException.class)
    public void testGetCollectionContextNull() {
        CollectionFactory factory = new CollectionFactory();
        factory.getCollection("test", null, false);
    }
}
