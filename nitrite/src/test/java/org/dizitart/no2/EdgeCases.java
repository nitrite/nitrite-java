package org.dizitart.no2;

import org.dizitart.no2.objects.BaseObjectRepositoryTest;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.data.Employee;
import org.dizitart.no2.objects.data.Note;
import org.dizitart.no2.objects.data.WithNitriteId;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Anindya Chatterjee
 */
public class EdgeCases extends BaseObjectRepositoryTest {

    @Test
    public void testDeleteIteratorNPE() {
        ObjectRepository<Note> notes = db.getRepository(Note.class);
        Note one = new Note();
        one.setText("Jane");
        one.setNoteId(1L);
        Note two = new Note();
        two.setText("Jill");
        two.setNoteId(2L);

        notes.insert(one, two);

        WriteResult writeResult = notes.remove(ObjectFilters.eq("text", "Pete"));
        for (NitriteId id : writeResult) {
            assertNotNull(id);
        }
    }

    @Test
    public void testDelete() {
        ObjectRepository<WithNitriteId> repo = db.getRepository(WithNitriteId.class);
        WithNitriteId one = new WithNitriteId();
        one.setName("Jane");
        repo.insert(one);

        WithNitriteId note = repo.find().firstOrDefault();
        repo.remove(note);

        assertNull(repo.getById(one.idField));
    }
}
