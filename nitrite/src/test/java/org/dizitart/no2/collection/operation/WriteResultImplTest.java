package org.dizitart.no2.collection.operation;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;

import org.dizitart.no2.collection.NitriteId;
import org.junit.Test;

public class WriteResultImplTest {
    @Test
    public void testSetNitriteIds() {
        WriteResultImpl writeResultImpl = new WriteResultImpl();
        writeResultImpl.setNitriteIds(new HashSet<NitriteId>());
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
        writeResultImpl.setNitriteIds(new HashSet<NitriteId>());
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

