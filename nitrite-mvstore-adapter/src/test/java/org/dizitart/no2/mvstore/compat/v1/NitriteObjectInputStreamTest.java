/*
 * Copyright (c) 2019-2020. Nitrite author or authors.
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

package org.dizitart.no2.mvstore.compat.v1;

import com.evil.EvilGadget;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedHashMap;

import static org.junit.Assert.*;

/**
 * Regression test for GHSA-9297-g93h-86gg: the legacy v1 migration deserializer
 * must reject arbitrary (potentially gadget) classes on the classpath.
 */
public class NitriteObjectInputStreamTest {

    private static byte[] serialize(Object obj) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
        }
        return bos.toByteArray();
    }

    @Test
    public void rejectsUnknownClass() throws Exception {
        EvilGadget.readObjectCalled = false;
        byte[] payload = serialize(new EvilGadget());

        try {
            NitriteDataType.deserialize(payload);
            fail("expected deserialization of a non-allowlisted class to be rejected");
        } catch (IllegalArgumentException expected) {
            // filter rejected the class before its readObject() could run
        }
        assertFalse("gadget readObject() must not be invoked", EvilGadget.readObjectCalled);
    }

    @Test
    public void allowsStandardTypes() throws Exception {
        assertEquals("hello", NitriteDataType.deserialize(serialize("hello")));

        LinkedHashMap<String, Object> doc = new LinkedHashMap<>();
        doc.put("k", 42L);
        assertEquals(doc, NitriteDataType.deserialize(serialize(doc)));
    }
}
