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

package org.dizitart.no2.transaction;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class UndoEntryTest {
    @Test
    public void testCanEqual() {
        assertFalse((new UndoEntry()).canEqual("Other"));
    }

    @Test
    public void testCanEqual2() {
        UndoEntry undoEntry = new UndoEntry();
        assertTrue(undoEntry.canEqual(new UndoEntry()));
    }

    @Test
    public void testConstructor() {
        UndoEntry actualUndoEntry = new UndoEntry();
        actualUndoEntry.setCollectionName("Collection Name");
        actualUndoEntry.setRollback(mock(Command.class));
        assertEquals("Collection Name", actualUndoEntry.getCollectionName());
    }

    @Test
    public void testEquals() {
        assertFalse((new UndoEntry()).equals("42"));
    }

    @Test
    public void testEquals2() {
        UndoEntry undoEntry = new UndoEntry();
        assertTrue(undoEntry.equals(new UndoEntry()));
    }

    @Test
    public void testEquals3() {
        UndoEntry undoEntry = new UndoEntry();
        undoEntry.setRollback(mock(Command.class));
        assertFalse(undoEntry.equals(new UndoEntry()));
    }

    @Test
    public void testEquals4() {
        UndoEntry undoEntry = new UndoEntry();

        UndoEntry undoEntry1 = new UndoEntry();
        undoEntry1.setRollback(mock(Command.class));
        assertFalse(undoEntry.equals(undoEntry1));
    }

    @Test
    public void testEquals5() {
        UndoEntry undoEntry = new UndoEntry();
        undoEntry.setCollectionName("Collection Name");
        assertFalse(undoEntry.equals(new UndoEntry()));
    }

    @Test
    public void testEquals6() {
        UndoEntry undoEntry = new UndoEntry();

        UndoEntry undoEntry1 = new UndoEntry();
        undoEntry1.setCollectionName("Collection Name");
        assertFalse(undoEntry.equals(undoEntry1));
    }

    @Test
    public void testEquals7() {
        UndoEntry undoEntry = new UndoEntry();
        undoEntry.setCollectionName("Collection Name");

        UndoEntry undoEntry1 = new UndoEntry();
        undoEntry1.setCollectionName("Collection Name");
        assertTrue(undoEntry.equals(undoEntry1));
    }

    @Test
    public void testHashCode() {
        assertEquals(6061, (new UndoEntry()).hashCode());
    }

    @Test
    public void testHashCode3() {
        UndoEntry undoEntry = new UndoEntry();
        undoEntry.setCollectionName("Collection Name");
        assertEquals(244972291, undoEntry.hashCode());
    }
}

