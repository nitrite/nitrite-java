package org.dizitart.no2.tx;

import org.dizitart.no2.DbTestOperations;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.Retry;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.TransactionalCollection;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.exceptions.TransactionException;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.TransactionalRepository;
import org.dizitart.no2.repository.data.DataGenerator;
import org.dizitart.no2.repository.data.Employee;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
public class TransactionTest {
    protected final String fileName = DbTestOperations.getRandomTempDbFile();
    protected Nitrite db;

    @Rule
    public Retry retry = new Retry(3);

    @Before
    public void setUp() {
        MVStoreModule storeModule = MVStoreModule.withConfig()
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

    @Test
    public void testConcurrentAddAndRemove() {
        ObjectRepository<Employee> repository = db.getRepository(Employee.class);

        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 5; i++) {
            executorService.submit(() -> {
                TransactionalRepository<Employee> transaction = repository.beginTransaction();
                try {
                    Employee employee = DataGenerator.generateEmployee();
                    Long empId = employee.getEmpId();
                    WriteResult result = transaction.insert(employee);
                    assertEquals(result.getAffectedCount(), 1);
                    assertEquals(transaction.size(), 1);

                    transaction.remove(where("empId").eq(empId));
                    assertEquals(transaction.size(), 0);

                    transaction.commit();
                } catch (Throwable t) {
                    transaction.rollback();
                    t.printStackTrace();
                    fail();
                }
            });
        }

        assertEquals(repository.size(), 0);
    }

    @Test
    public void testConcurrentAdd() {
        NitriteCollection collection = db.getCollection("testConcurrentAdd");

        TransactionalCollection tx1 = collection.beginTransaction();
        TransactionalCollection tx2 = collection.beginTransaction();

        tx1.insert(Document.createDocument("firstName", "John"));
        tx2.insert(Document.createDocument("firstName", "Jane"));

        assertEquals(tx1.size(), 1);
        assertEquals(tx2.size(), 1);
        assertEquals(collection.size(), 0);

        tx1.commit();
        assertEquals(collection.size(), 1);

        tx2.commit();
        assertEquals(collection.size(), 2);
    }

    @Test
    public void testConcurrentUpdate() {
        NitriteCollection collection = db.getCollection("testConcurrentUpdate");
        Document doc1 = Document.createDocument("firstName", "John");
        doc1.getId();
        Document doc2 = doc1.clone();

        collection.insert(doc1);

        TransactionalCollection tx1 = collection.beginTransaction();
        TransactionalCollection tx2 = collection.beginTransaction();

        doc1.put("firstName", "Ram");
        tx1.update(doc1);

        doc2.put("firstName", "Rohit");
        tx2.update(doc2);

        tx1.commit();
        assertEquals(collection.find().firstOrNull().get("firstName"), "Ram");

        tx2.commit();
        assertEquals(collection.find().firstOrNull().get("firstName"), "Rohit");
    }

    @Test
    public void testRollbackOnFailure() {
        NitriteCollection collection = db.getCollection("testRollbackOnFailure");
        TransactionalCollection tx = collection.beginTransaction();

        int failureCount = 0;
        try {
            Document doc1 = Document.createDocument("firstName", "John");
            doc1.getId();

            tx.insert(doc1);
            tx.insert(doc1);
        } catch (Exception e) {
            tx.rollback();
            failureCount = 1;
        }
        assertEquals(failureCount, 1);
        assertEquals(collection.size(), 0);
    }

    @Test
    public void testTransactionTryResourceRollback() {
        NitriteCollection collection = db.getCollection("testRollbackOnFailure");

        int failureCount = 0;
        try(TransactionalCollection tx = collection.beginTransaction()) {
            Document doc1 = Document.createDocument("firstName", "John");
            doc1.getId();

            tx.insert(doc1);
            tx.insert(doc1);
        } catch (Exception e) {
            // auto rollback
            failureCount = 1;
        }
        assertEquals(failureCount, 1);
        assertEquals(collection.size(), 0);
    }

    @Test
    public void testTransactionOnDifferentCollections() {
        NitriteCollection col1 = db.getCollection("col1");
        NitriteCollection col2 = db.getCollection("col2");

        TransactionalCollection tx1 = col1.beginTransaction();
        TransactionalCollection tx2 = col2.beginTransaction();

        tx1.insert(Document.createDocument("name", "col1"));
        tx2.insert(Document.createDocument("name", "col2"));

        tx2.commit();
        tx1.commit();

        assertEquals(col1.find(where("name").eq("col1")).size(), 1);
        assertEquals(col2.find(where("name").eq("col2")).size(), 1);
    }

    @Test(expected = TransactionException.class)
    public void testFailureOnClosedTransaction() {
        NitriteCollection collection = db.getCollection("testFailureOnClosedTransaction");
        TransactionalCollection tx = collection.beginTransaction();
        tx.rollback();

        tx.insert(Document.createDocument());
    }
}
