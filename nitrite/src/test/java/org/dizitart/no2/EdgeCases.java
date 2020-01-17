package org.dizitart.no2;

import org.dizitart.no2.objects.BaseObjectRepositoryTest;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.data.Employee;
import org.dizitart.no2.objects.data.Note;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

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
        Employee employee = employeeRepository.find().firstOrDefault();
    }
}
