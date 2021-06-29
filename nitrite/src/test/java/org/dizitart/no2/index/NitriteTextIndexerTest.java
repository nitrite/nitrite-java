package org.dizitart.no2.index;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.FindPlan;
import org.dizitart.no2.common.FieldValues;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.index.fulltext.EnglishTextTokenizer;
import org.dizitart.no2.store.memory.InMemoryStore;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NitriteTextIndexerTest {
    @Test
    public void testConstructor() {
        assertEquals(IndexType.FULL_TEXT, (new NitriteTextIndexer(new EnglishTextTokenizer())).getIndexType());
        assertEquals("Fulltext", (new NitriteTextIndexer()).getIndexType());
    }

    @Test
    public void testGetIndexType() {
        assertEquals("Fulltext", (new NitriteTextIndexer()).getIndexType());
    }

    @Test
    public void testInitialize() {
        NitriteTextIndexer nitriteTextIndexer = new NitriteTextIndexer();
        NitriteConfig nitriteConfig = new NitriteConfig();
        nitriteTextIndexer.initialize(nitriteConfig);
        assertEquals(1, nitriteConfig.getSchemaVersion().intValue());
    }

    @Test
    public void testValidateIndex() {
        NitriteTextIndexer nitriteTextIndexer = new NitriteTextIndexer();
        Fields fields = new Fields();
        nitriteTextIndexer.validateIndex(fields);
        assertEquals("[]", fields.toString());
    }

    @Test
    public void testValidateIndex2() {
        NitriteTextIndexer nitriteTextIndexer = new NitriteTextIndexer();

        Fields fields = new Fields();
        fields.addField("Field");
        nitriteTextIndexer.validateIndex(fields);
        assertEquals("[Field]", fields.toString());
    }

    @Test
    public void testValidateIndex3() {
        NitriteTextIndexer nitriteTextIndexer = new NitriteTextIndexer();

        Fields fields = new Fields();
        fields.addField("Field");
        fields.addField("Field");
        assertThrows(IndexingException.class, () -> nitriteTextIndexer.validateIndex(fields));
    }

    @Test(expected = IndexingException.class)
    public void testDropIndex() {
        NitriteTextIndexer nitriteTextIndexer = new NitriteTextIndexer();
        Fields fields = mock(Fields.class);
        when(fields.getEncodedName()).thenThrow(new IndexingException("An error occurred"));
        IndexDescriptor indexDescriptor = new IndexDescriptor("Index Type", fields, "Collection Name");
        nitriteTextIndexer.dropIndex(indexDescriptor, new NitriteConfig());
        verify(fields).getEncodedName();
        assertFalse(indexDescriptor.isCompoundIndex());
    }

    @Test(expected = IndexingException.class)
    public void testDropIndex2() {
        NitriteTextIndexer nitriteTextIndexer = new NitriteTextIndexer();
        IndexDescriptor indexDescriptor = mock(IndexDescriptor.class);
        when(indexDescriptor.getIndexType()).thenThrow(new IndexingException("An error occurred"));
        when(indexDescriptor.getIndexFields()).thenReturn(new Fields());
        when(indexDescriptor.getCollectionName()).thenReturn("foo");
        nitriteTextIndexer.dropIndex(indexDescriptor, new NitriteConfig());
        verify(indexDescriptor).getIndexType();
        verify(indexDescriptor).getIndexFields();
        verify(indexDescriptor).getCollectionName();
    }

    @Test
    public void testDropIndex3() {
        NitriteTextIndexer nitriteTextIndexer = new NitriteTextIndexer();
        IndexDescriptor indexDescriptor = mock(IndexDescriptor.class);
        when(indexDescriptor.getIndexType()).thenReturn("foo");
        when(indexDescriptor.getIndexFields()).thenReturn(new Fields());
        when(indexDescriptor.getCollectionName()).thenReturn("foo");
        NitriteConfig nitriteConfig = mock(NitriteConfig.class);
        doReturn(new InMemoryStore()).when(nitriteConfig).getNitriteStore();
        nitriteTextIndexer.dropIndex(indexDescriptor, nitriteConfig);
        verify(indexDescriptor).getIndexType();
        verify(indexDescriptor).getIndexFields();
        verify(indexDescriptor).getCollectionName();
        verify(nitriteConfig).getNitriteStore();
    }

    @Test
    public void testWriteIndexEntry() {
        NitriteTextIndexer nitriteTextIndexer = new NitriteTextIndexer();
        FieldValues fieldValues = new FieldValues();
        fieldValues.getValues().add(Pair.pair("a", "1"));
        IndexDescriptor indexDescriptor = new IndexDescriptor("Index Type", Fields.withNames("a"), "Collection Name");
        NitriteConfig nitriteConfig = mock(NitriteConfig.class);
        doReturn(new InMemoryStore()).when(nitriteConfig).getNitriteStore();
        nitriteTextIndexer.writeIndexEntry(fieldValues, indexDescriptor, nitriteConfig);
        assertEquals("FieldValues(nitriteId=null, fields=[a], values=[Pair(first=a, second=1)])", fieldValues.toString());
        assertEquals("[a]", fieldValues.getFields().toString());
    }

    @Test
    public void testRemoveIndexEntry() {
        NitriteTextIndexer nitriteTextIndexer = new NitriteTextIndexer();
        FieldValues fieldValues = new FieldValues();
        fieldValues.getValues().add(Pair.pair("a", "1"));
        IndexDescriptor indexDescriptor = new IndexDescriptor("Index Type", Fields.withNames("a"), "Collection Name");
        NitriteConfig nitriteConfig = mock(NitriteConfig.class);
        doReturn(new InMemoryStore()).when(nitriteConfig).getNitriteStore();
        nitriteTextIndexer.removeIndexEntry(fieldValues, indexDescriptor, nitriteConfig);
        assertEquals("FieldValues(nitriteId=null, fields=[a], values=[Pair(first=a, second=1)])", fieldValues.toString());
        assertEquals("[a]", fieldValues.getFields().toString());
    }

    @Test
    public void testFindByFilter() {
        NitriteTextIndexer nitriteTextIndexer = new NitriteTextIndexer();

        FindPlan findPlan = new FindPlan();
        findPlan.setIndexDescriptor(new IndexDescriptor("Index Type", new Fields(), "Collection Name"));
        assertTrue(nitriteTextIndexer.findByFilter(findPlan, new NitriteConfig()).isEmpty());
    }

    @Test
    public void testFindByFilter2() {
        NitriteTextIndexer nitriteTextIndexer = new NitriteTextIndexer();

        FindPlan findPlan = new FindPlan();
        findPlan.setIndexScanFilter(null);
        findPlan.setIndexDescriptor(new IndexDescriptor("Index Type", new Fields(), "Collection Name"));
        assertTrue(nitriteTextIndexer.findByFilter(findPlan, new NitriteConfig()).isEmpty());
    }
}

