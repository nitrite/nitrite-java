package org.dizitart.no2;

import org.dizitart.no2.mapper.Employee;
import org.dizitart.no2.objects.BaseObjectRepositoryTest;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author Anindya Chatterjee
 */
public class EdgeCases extends BaseObjectRepositoryTest {

    @Test
    public void testDeleteIteratorNPE() {
        ObjectRepository<Employee> emps = db.getRepository(Employee.class);
        Employee one = new Employee();
        one.setName("Jane");
        one.setEmpId("1");
        Employee two = new Employee();
        two.setName("Jill");
        two.setEmpId("2");

        emps.insert(one, two);

        WriteResult writeResult = emps.remove(ObjectFilters.eq("name", "Pete"));
        for (NitriteId id : writeResult) {
            assertNotNull(id);
        }
    }

    @Test
    public void testDelete() {

    }
}
