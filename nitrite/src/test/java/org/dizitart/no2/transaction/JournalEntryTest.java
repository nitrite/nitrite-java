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

public class JournalEntryTest {
    @Test
    public void testCanEqual() {
        assertFalse((new JournalEntry()).canEqual("Other"));
    }

    @Test
    public void testCanEqual2() {
        JournalEntry journalEntry = new JournalEntry();
        assertTrue(journalEntry.canEqual(new JournalEntry()));
    }

    @Test
    public void testConstructor() {
        JournalEntry actualJournalEntry = new JournalEntry();
        actualJournalEntry.setChangeType(ChangeType.Insert);
        actualJournalEntry.setCommit(mock(Command.class));
        actualJournalEntry.setRollback(mock(Command.class));
        assertEquals(ChangeType.Insert, actualJournalEntry.getChangeType());
    }

    @Test
    public void testConstructor2() {
        JournalEntry actualJournalEntry = new JournalEntry(ChangeType.Insert, mock(Command.class), mock(Command.class));
        actualJournalEntry.setChangeType(ChangeType.Insert);
        actualJournalEntry.setCommit(mock(Command.class));
        actualJournalEntry.setRollback(mock(Command.class));
        assertEquals(ChangeType.Insert, actualJournalEntry.getChangeType());
    }

    @Test
    public void testEquals() {
        assertFalse((new JournalEntry()).equals("42"));
    }

    @Test
    public void testEquals2() {
        JournalEntry journalEntry = new JournalEntry();
        assertTrue(journalEntry.equals(new JournalEntry()));
    }

    @Test
    public void testEquals3() {
        JournalEntry journalEntry = new JournalEntry();
        assertFalse(journalEntry.equals(new JournalEntry(ChangeType.Insert, mock(Command.class), mock(Command.class))));
    }

    @Test
    public void testEquals4() {
        JournalEntry journalEntry = new JournalEntry(ChangeType.Insert, mock(Command.class), mock(Command.class));
        assertFalse(journalEntry.equals(new JournalEntry()));
    }

    @Test
    public void testEquals5() {
        JournalEntry journalEntry = new JournalEntry(ChangeType.Insert, mock(Command.class), mock(Command.class));
        assertFalse(journalEntry.equals(new JournalEntry(ChangeType.Insert, mock(Command.class), mock(Command.class))));
    }

    @Test
    public void testEquals6() {
        JournalEntry journalEntry = new JournalEntry();
        assertFalse(journalEntry.equals(new JournalEntry(null, mock(Command.class), mock(Command.class))));
    }

    @Test
    public void testEquals7() {
        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setRollback(mock(Command.class));
        assertFalse(journalEntry.equals(new JournalEntry()));
    }

    @Test
    public void testEquals8() {
        JournalEntry journalEntry = new JournalEntry();

        JournalEntry journalEntry1 = new JournalEntry();
        journalEntry1.setRollback(mock(Command.class));
        assertFalse(journalEntry.equals(journalEntry1));
    }

    @Test
    public void testHashCode() {
        assertEquals(357642, (new JournalEntry()).hashCode());
    }

    @Test
    public void testHashCode2() {
        (new JournalEntry(ChangeType.Insert, mock(Command.class), mock(Command.class))).hashCode();
    }
}

