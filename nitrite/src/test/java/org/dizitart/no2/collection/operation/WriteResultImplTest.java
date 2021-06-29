package org.dizitart.no2.collection.operation;

import org.dizitart.no2.collection.NitriteId;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class WriteResultImplTest {
    @Test
    public void testSetNitriteIds() {
        WriteResultImpl writeResultImpl = new WriteResultImpl();
        writeResultImpl.setNitriteIds(new HashSet<>());
        assertEquals("WriteResultImpl(nitriteIds=[])", writeResultImpl.toString());
    }

    @Test
    public void testAddToList() {
        WriteResultImpl writeResultImpl = new WriteResultImpl();
        writeResultImpl.addToList(NitriteId.newId());
        assertEquals(1, writeResultImpl.getAffectedCount());
    }

    @Test
    public void testAddToList2() {
        WriteResultImpl writeResultImpl = new WriteResultImpl();
        writeResultImpl.setNitriteIds(new HashSet<>());
        writeResultImpl.addToList(NitriteId.newId());
        assertEquals(1, writeResultImpl.getAffectedCount());
    }

    @Test
    public void testGetAffectedCount() {
        assertEquals(0, (new WriteResultImpl()).getAffectedCount());
    }

    @Test
    public void testGetAffectedCount2() {
        WriteResultImpl writeResultImpl = new WriteResultImpl();
        writeResultImpl.addToList(null);
        assertEquals(1, writeResultImpl.getAffectedCount());
    }
}

