/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
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

package org.dizitart.no2.common.util;

import org.dizitart.no2.common.Fields;
import org.dizitart.no2.index.IndexDescriptor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IndexUtilsTest {
    @Test
    public void testDeriveIndexMapName() {
        assertEquals("$nitrite_index|Collection Name||Index Type",
            IndexUtils.deriveIndexMapName(new IndexDescriptor("Index Type", new Fields(), "Collection Name")));
    }

    @Test
    public void testDeriveIndexMapName2() {
        IndexDescriptor indexDescriptor = new IndexDescriptor("Index Type", new Fields(), "Collection Name");
        indexDescriptor.setIndexFields(new Fields());
        assertEquals("$nitrite_index|Collection Name||Index Type", IndexUtils.deriveIndexMapName(indexDescriptor));
    }

    @Test
    public void testDeriveIndexMetaMapName() {
        assertEquals("$nitrite_index_meta|Collection Name", IndexUtils.deriveIndexMetaMapName("Collection Name"));
    }
}

