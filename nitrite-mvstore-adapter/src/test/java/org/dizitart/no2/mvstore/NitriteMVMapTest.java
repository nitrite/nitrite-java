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

package org.dizitart.no2.mvstore;

import org.h2.mvstore.MVMap;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class NitriteMVMapTest {
    @Test
    public void testValues() {
        NitriteMVMap<Object, Object> nitriteMVMap = new NitriteMVMap<>(
            (MVMap<Object, Object>) mock(MVMap.class), null);
        assertTrue(nitriteMVMap.values().toList().isEmpty());
        assertFalse(nitriteMVMap.isEmpty());
    }

    @Test
    public void testKeys() {
        MVMap<Object, Object> objectObjectMap = (MVMap<Object, Object>) mock(MVMap.class);
        when(objectObjectMap.keySet()).thenReturn(new HashSet<>());
        NitriteMVMap<Object, Object> nitriteMVMap = new NitriteMVMap<>(objectObjectMap, null);
        assertTrue(nitriteMVMap.keys().toList().isEmpty());
        verify(objectObjectMap).keySet();
        assertFalse(nitriteMVMap.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor() {
        NitriteMVMap<Object, Object> actualNitriteMVMap = new NitriteMVMap<>(
            (MVMap<Object, Object>) mock(MVMap.class), null);
        actualNitriteMVMap.close();
        assertFalse(actualNitriteMVMap.isEmpty());
    }

    @Test
    public void testIsEmpty() {
        MVMap<Object, Object> objectObjectMap = (MVMap<Object, Object>) mock(MVMap.class);
        when(objectObjectMap.isEmpty()).thenReturn(true);
        assertTrue((new NitriteMVMap<>(objectObjectMap, null)).isEmpty());
        verify(objectObjectMap).isEmpty();
    }
}

