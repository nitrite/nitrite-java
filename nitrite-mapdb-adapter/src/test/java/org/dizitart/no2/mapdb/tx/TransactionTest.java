package org.dizitart.no2.mapdb.tx;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.mapdb.DbTestOperations;
import org.dizitart.no2.mapdb.MapDBModule;
import org.dizitart.no2.mapdb.repository.data.DataGenerator;
import org.dizitart.no2.mapdb.repository.data.Employee;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.TransactionalRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
public class TransactionTest {
    protected final String fileName = DbTestOperations.getRandomTempDbFile();
    protected Nitrite db;

    @Before
    public void setUp() {
        MapDBModule storeModule = MapDBModule.withConfig()
            .filePath(fileName)
            .build();

        db = Nitrite.builder()
            .loadModule(storeModule)
            .fieldSeparator(".")
            .openOrCreate();
    }

    @After
    public void cleanUp() throws IOException {
        if (db != null && !db.isClosed()) {
            db.close();
        }
        Files.delete(Paths.get(fileName));
    }

    @Test
    public void testTransactionCommit() {
        ObjectRepository<Employee> repository = db.getRepository(Employee.class);
        TransactionalRepository<Employee> transaction = repository.beginTransaction();

        Employee employee = DataGenerator.generateEmployee();
        Long empId = employee.getEmpId();
        transaction.insert(employee);

        // check in primary repository if employee is there.
        Employee byId = repository.getById(empId);
        assertNull(byId);

        // check in transactional repo
        byId = transaction.getById(empId);
        assertNotNull(byId);
        assertEquals(byId, employee);

        transaction.commit();

        byId = repository.getById(empId);
        assertNotNull(byId);
        assertEquals(byId, employee);

        transaction = repository.beginTransaction();
        transaction.remove(where("empId").eq(empId));

        assertEquals(repository.size(), 1);

        transaction.commit();
        assertEquals(repository.size(), 0);
    }

    @Test
    public void testTransactionRollback() {
        ObjectRepository<Employee> repository = db.getRepository(Employee.class);
        TransactionalRepository<Employee> transaction = repository.beginTransaction();

        Employee employee = DataGenerator.generateEmployee();
        Long empId = employee.getEmpId();
        transaction.insert(employee);

        assertEquals(repository.size(), 0);
        assertEquals(transaction.size(), 1);

        transaction.rollback();
        assertEquals(repository.size(), 0);

        repository.insert(employee);
        transaction = repository.beginTransaction();
        transaction.remove(where("empId").eq(empId));

        transaction.rollback();
        assertEquals(repository.size(), 1);
    }
}
