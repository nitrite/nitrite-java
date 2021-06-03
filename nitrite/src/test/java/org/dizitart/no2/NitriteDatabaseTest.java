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

package org.dizitart.no2;

import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.SecurityException;
import org.junit.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class NitriteDatabaseTest {
    @Test
    public void testConstructor() {
        NitriteConfig nitriteConfig = new NitriteConfig();
        new NitriteDatabase("janedoe", "iloveyou", nitriteConfig);
        assertTrue(nitriteConfig.configured);
    }

    @Test
    public void testConstructor2() {
        assertThrows(SecurityException.class, () -> new NitriteDatabase("", "iloveyou", new NitriteConfig()));
    }

    @Test
    public void testConstructor3() {
        assertThrows(SecurityException.class, () -> new NitriteDatabase("janedoe", "", new NitriteConfig()));
    }

    @Test
    public void testConstructor4() {
        assertThrows(NitriteIOException.class, () -> new NitriteDatabase("janedoe", "iloveyou", null));
    }

    @Test
    public void testConstructor5() {
        NitriteConfig nitriteConfig = new NitriteConfig();
        new NitriteDatabase(nitriteConfig);
        assertTrue(nitriteConfig.configured);
    }

    @Test
    public void testConstructor6() {
        assertThrows(NitriteIOException.class, () -> new NitriteDatabase(null));
    }
}

