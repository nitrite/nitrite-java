/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.rocksdb.tx;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.Cursor;
import org.dizitart.no2.repository.TransactionalRepository;
import org.dizitart.no2.rocksdb.repository.data.*;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import static org.awaitility.Awaitility.await;
import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.*;


/**
 * @author Anindya Chatterjee.
 */
public class RepositoryModificationTest extends BaseTransactionalRepositoryTest {

    @Test
    public void testCreateIndex() {
        assertTrue(txCompanyRepository.hasIndex("companyName"));
        assertFalse(txCompanyRepository.hasIndex("dateCreated"));

        txCompanyRepository.createIndex("dateCreated", IndexOptions.indexOptions(IndexType.NonUnique));
        assertTrue(txCompanyRepository.hasIndex("dateCreated"));
        assertFalse(txCompanyRepository.isIndexing("dateCreated"));

        txCompanyRepository.commit();
        assertTrue(companyRepository.hasIndex("companyName"));
        assertTrue(companyRepository.hasIndex("dateCreated"));
    }

    @Test
    public void testRebuildIndex() {
        txCompanyRepository.createIndex("dateCreated", IndexOptions.indexOptions(IndexType.NonUnique));
        assertFalse(txCompanyRepository.isIndexing("dateCreated"));

        txCompanyRepository.rebuildIndex("dateCreated", true);
        assertTrue(txCompanyRepository.isIndexing("dateCreated"));

        await().until(() -> !txCompanyRepository.isIndexing("dateCreated"));

        assertFalse(companyRepository.hasIndex("dateCreated"));
        txCompanyRepository.commit();
        assertTrue(companyRepository.hasIndex("dateCreated"));
    }

    @Test
    public void testListIndexes() {
        Collection<IndexEntry> indices = companyRepository.listIndices();
        assertEquals(indices.size(), 2);

        txCompanyRepository.createIndex("dateCreated", IndexOptions.indexOptions(IndexType.NonUnique));
        txCompanyRepository.commit();

        indices = companyRepository.listIndices();
        assertEquals(indices.size(), 3);
    }

    @Test
    public void testDropIndex() {
        testListIndexes();
        txCompanyRepository = companyRepository.beginTransaction();
        txCompanyRepository.dropIndex("dateCreated");
        txCompanyRepository.commit();
        Collection<IndexEntry> indices = companyRepository.listIndices();
        assertEquals(indices.size(), 2);
    }

    @Test
    public void testDropAllIndex() {
        testListIndexes();
        txCompanyRepository = companyRepository.beginTransaction();
        txCompanyRepository.dropAllIndices();
        txCompanyRepository.commit();
        Collection<IndexEntry> indices = companyRepository.listIndices();
        assertEquals(indices.size(), 0);
    }

    @Test
    public void testCompanyRecord() {
        Cursor<Company> cursor = companyRepository.find();
        assertEquals(cursor.size(), 0);
        txCompanyRepository.commit();

        cursor = companyRepository.find();
        assertEquals(cursor.size(), 10);
        assertFalse(cursor.isEmpty());
    }

    @Test
    public void testInsert() {
        Company company = DataGenerator.generateCompanyRecord();
        Cursor<Company> cursor = txCompanyRepository.find();
        assertEquals(cursor.size(), 10);

        txCompanyRepository.insert(company);
        cursor = txCompanyRepository.find();
        assertEquals(cursor.size(), 11);

        Company company1 = DataGenerator.generateCompanyRecord();
        Company company2 = DataGenerator.generateCompanyRecord();
        txCompanyRepository.insert(new Company[]{company1, company2});

        txCompanyRepository.commit();
        cursor = companyRepository.find();
        assertEquals(cursor.size(), 13);
    }

    @Test
    public void testUpdateWithFilter() {
        txEmployeeRepository.remove(Filter.ALL);

        Employee employee = new Employee();
        employee.setCompany(null);
        employee.setAddress("abcd road");
        employee.setBlob(new byte[]{1, 2, 125});
        employee.setEmpId(12L);
        employee.setJoinDate(new Date());
        Note empNote = new Note();
        empNote.setNoteId(23L);
        empNote.setText("sample text note");
        employee.setEmployeeNote(empNote);

        txEmployeeRepository.insert(employee);
        Cursor<Employee> result = txEmployeeRepository.find();
        assertEquals(result.size(), 1);
        for (Employee e : result) {
            assertEquals(e.getAddress(), "abcd road");
        }

        Employee updated = new Employee(employee);
        updated.setAddress("xyz road");
        WriteResult writeResult = txEmployeeRepository.update(where("empId").eq(12L), updated);
        assertEquals(writeResult.getAffectedCount(), 1);

        txEmployeeRepository.commit();
        result = employeeRepository.find();
        assertEquals(result.size(), 1);
        for (Employee e : result) {
            assertEquals(e.getAddress(), "xyz road");
        }
    }

    @Test
    public void testUpdateWithJustOnceFalse() throws ParseException {
        Date joiningDate = new Date();
        prepareUpdateWithOptions(joiningDate);

        SimpleDateFormat simpleDateFormat
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        Date newJoiningDate = simpleDateFormat.parse("2012-07-01T16:02:48.440Z");

        Document updated1 = createDocument();
        updated1.put("joinDate", newJoiningDate);

        WriteResult writeResult
            = txEmployeeRepository.update(where("empId").eq(12L), updated1, false);
        assertEquals(writeResult.getAffectedCount(), 1);

        Cursor<Employee> result = txEmployeeRepository.find(where("joinDate").eq(joiningDate));
        assertEquals(result.size(), 1);
        result = txEmployeeRepository.find(where("joinDate").eq(newJoiningDate));
        assertEquals(result.size(), 1);

        txEmployeeRepository.remove(Filter.ALL);
        prepareUpdateWithOptions(joiningDate);
        result = txEmployeeRepository.find();
        assertEquals(result.size(), 2);

        Document update = createDocument();
        update.put("joinDate", newJoiningDate);

        writeResult = txEmployeeRepository.update(where("joinDate").eq(joiningDate), update, false);
        assertEquals(writeResult.getAffectedCount(), 2);

        result = txEmployeeRepository.find(where("joinDate").eq(joiningDate));
        assertEquals(result.size(), 0);

        txEmployeeRepository.commit();
        result = employeeRepository.find(where("joinDate").eq(newJoiningDate));
        assertEquals(result.size(), 2);
    }

    @Test
    public void testUpsertTrue() {
        Date joiningDate = new Date();
        Cursor<Employee> result = txEmployeeRepository.find(where("joinDate").eq(joiningDate));
        assertEquals(result.size(), 0);

        Employee employee = new Employee();
        employee.setCompany(null);
        employee.setAddress("some road");
        employee.setBlob(new byte[]{1, 2, 125});
        employee.setEmpId(12L);
        employee.setJoinDate(joiningDate);
        Note empNote1 = new Note();
        empNote1.setNoteId(23L);
        empNote1.setText("sample text note");
        employee.setEmployeeNote(empNote1);

        WriteResult writeResult
            = txEmployeeRepository.update(where("empId").eq(12L), employee, true);
        assertEquals(writeResult.getAffectedCount(), 1);

        txEmployeeRepository.commit();
        result = employeeRepository.find(where("joinDate").eq(joiningDate));
        assertEquals(result.size(), 1);
    }

    @Test
    public void testUpsertFalse() {
        Date joiningDate = new Date();
        Cursor<Employee> result = txEmployeeRepository.find(where("joinDate").eq(joiningDate));
        assertEquals(result.size(), 0);

        Employee employee = new Employee();
        employee.setCompany(null);
        employee.setAddress("some road");
        employee.setBlob(new byte[]{1, 2, 125});
        employee.setEmpId(12L);
        employee.setJoinDate(joiningDate);
        Note empNote1 = new Note();
        empNote1.setNoteId(23L);
        empNote1.setText("sample text note");
        employee.setEmployeeNote(empNote1);

        WriteResult writeResult
            = txEmployeeRepository.update(where("empId").eq(12L), employee, false);
        assertEquals(writeResult.getAffectedCount(), 0);

        txEmployeeRepository.commit();
        result = employeeRepository.find(where("joinDate").eq(joiningDate));
        assertEquals(result.size(), 0);
    }

    @Test
    public void testDeleteFilterAndWithOutOption() {
        Date joiningDate = new Date();
        prepareUpdateWithOptions(joiningDate);

        Cursor<Employee> result = txEmployeeRepository.find(where("joinDate").eq(joiningDate));
        assertEquals(result.size(), 2);

        WriteResult writeResult = txEmployeeRepository.remove(where("joinDate").eq(joiningDate));
        assertEquals(writeResult.getAffectedCount(), 2);

        txEmployeeRepository.commit();
        result = employeeRepository.find(where("joinDate").eq(joiningDate));
        assertEquals(result.size(), 0);
    }

    @Test
    public void testDeleteFilterAndWithOption() {
        Date joiningDate = new Date();
        prepareUpdateWithOptions(joiningDate);

        Cursor<Employee> result = txEmployeeRepository.find(where("joinDate").eq(joiningDate));
        assertEquals(result.size(), 2);

        WriteResult writeResult = txEmployeeRepository.remove(where("joinDate").eq(joiningDate), true);
        assertEquals(writeResult.getAffectedCount(), 1);

        txEmployeeRepository.commit();
        result = employeeRepository.find(where("joinDate").eq(joiningDate));
        assertEquals(result.size(), 1);
    }

    @Test
    public void testEmployeeRecord() {
        Iterable<Employee> totalResult = txEmployeeRepository.find();
        int occurrence = 0;
        for (Employee employee : totalResult) {
            if (employee.getEmployeeNote().getText().toLowerCase().contains("class aptent")) {
                occurrence++;
            }
        }

        txEmployeeRepository.commit();
        Cursor<Employee> cursor = employeeRepository.find(where("employeeNote.text").text("Class aptent"));
        assertEquals(cursor.size(), occurrence);
    }

    @Test
    public void testUpdateWithOptions() {
        Employee employee = txEmployeeRepository.find().firstOrNull();

        Document update = createDocument();
        update.put("address", "new address");

        WriteResult writeResult
            = txEmployeeRepository.update(where("empId").eq(employee.getEmpId()), update, false);
        assertEquals(writeResult.getAffectedCount(), 1);

        Employee byId = txEmployeeRepository.getById(employee.getEmpId());
        assertEquals(byId.getAddress(), "new address");
        assertEquals(byId.getEmpId(), employee.getEmpId());

        update.put("address", "another address");
        txEmployeeRepository.update(where("empId").eq(employee.getEmpId()), update);

        txEmployeeRepository.commit();
        byId = employeeRepository.getById(employee.getEmpId());
        assertEquals(byId.getAddress(), "another address");
        assertEquals(byId.getEmpId(), employee.getEmpId());
    }

    @Test(expected = InvalidIdException.class)
    public void testMultiUpdateWithObject() {
        txEmployeeRepository.remove(Filter.ALL);

        Date now = new Date();
        Employee employee1 = new Employee();
        employee1.setEmpId(1L);
        employee1.setAddress("abcd");
        employee1.setJoinDate(now);

        Employee employee2 = new Employee();
        employee2.setEmpId(2L);
        employee2.setAddress("xyz");
        employee2.setJoinDate(now);
        txEmployeeRepository.insert(employee1, employee2);

        Employee update = new Employee();
        update.setAddress("new address");

        WriteResult writeResult
            = txEmployeeRepository.update(where("joinDate").eq(now), update, false);
        assertEquals(writeResult.getAffectedCount(), 0);
    }

    @Test
    public void testUpdateNull() {
        Employee employee = txEmployeeRepository.find().firstOrNull();
        Employee newEmployee = new Employee(employee);
        newEmployee.setJoinDate(null);

        Employee result = txEmployeeRepository.find(where("empId").eq(employee.getEmpId())).firstOrNull();
        assertNotNull(result.getJoinDate());

        WriteResult writeResult = txEmployeeRepository.update(newEmployee, false);
        assertEquals(writeResult.getAffectedCount(), 1);

        txEmployeeRepository.commit();
        result = employeeRepository.find(where("empId").eq(employee.getEmpId())).firstOrNull();
        assertNull(result.getJoinDate());

        // update with object filter and item and set id different
    }

    @Test
    public void testUpdateWithChangedId() {
        Employee employee = txEmployeeRepository.find().firstOrNull();
        Long oldId = employee.getEmpId();
        long count = txEmployeeRepository.size();

        Employee newEmployee = new Employee(employee);
        newEmployee.setEmpId(50L);

        Employee result = txEmployeeRepository.find(where("empId").eq(oldId)).firstOrNull();
        assertNotNull(result.getJoinDate());

        WriteResult writeResult = txEmployeeRepository.update(where("empId").eq(oldId), newEmployee, false);
        assertEquals(writeResult.getAffectedCount(), 1);

        assertEquals(count, txEmployeeRepository.size());

        txEmployeeRepository.commit();
        Cursor<Employee> cursor = employeeRepository.find(where("empId").eq(oldId));
        assertEquals(cursor.size(), 0);
    }

    @Test(expected = InvalidIdException.class)
    public void testUpdateWithNullId() {
        Employee employee = txEmployeeRepository.find().firstOrNull();
        Long oldId = employee.getEmpId();

        Employee newEmployee = new Employee(employee);
        newEmployee.setEmpId(null);

        Employee result = txEmployeeRepository.find(where("empId").eq(oldId)).firstOrNull();
        assertNotNull(result.getJoinDate());

        txEmployeeRepository.commit();
        WriteResult writeResult = employeeRepository.update(where("empId").eq(oldId), newEmployee, false);
        assertEquals(writeResult.getAffectedCount(), 1);
    }

    @Test(expected = UniqueConstraintException.class)
    public void testUpdateWithDuplicateId() {
        Employee employee = txEmployeeRepository.find().firstOrNull();
        Long oldId = employee.getEmpId();
        long count = txEmployeeRepository.size();

        Employee newEmployee = new Employee(employee);
        newEmployee.setEmpId(5L);

        Employee result = txEmployeeRepository.find(where("empId").eq(oldId)).firstOrNull();
        assertNotNull(result.getJoinDate());

        WriteResult writeResult = txEmployeeRepository.update(where("empId").eq(oldId), newEmployee, false);
        assertEquals(writeResult.getAffectedCount(), 1);

        assertEquals(count, txEmployeeRepository.size());

        txEmployeeRepository.commit();
        Cursor<Employee> cursor = employeeRepository.find(where("empId").eq(oldId));
        assertEquals(cursor.size(), 0);
    }

    @Test
    public void testUpdateWithObject() {
        Employee employee = txEmployeeRepository.find().firstOrNull();
        Employee newEmployee = new Employee(employee);

        Long id = employee.getEmpId();
        String address = employee.getAddress();
        newEmployee.setAddress("new address");

        WriteResult writeResult = txEmployeeRepository.update(newEmployee);
        assertEquals(writeResult.getAffectedCount(), 1);

        txEmployeeRepository.commit();
        Employee emp = employeeRepository.find(where("empId").eq(id)).firstOrNull();
        assertNotEquals(address, emp.getAddress());
        assertEquals(employee.getEmpId(), emp.getEmpId());
        assertEquals(employee.getJoinDate(), emp.getJoinDate());
        assertArrayEquals(employee.getBlob(), emp.getBlob());
    }

    @Test
    public void testUpsertWithObject() {
        Employee employee = new Employee();
        employee.setCompany(null);
        employee.setAddress("some road");
        employee.setBlob(new byte[]{1, 2, 125});
        employee.setEmpId(12L);
        employee.setJoinDate(new Date());
        Note empNote = new Note();
        empNote.setNoteId(23L);
        empNote.setText("sample text note");
        employee.setEmployeeNote(empNote);

        WriteResult writeResult = txEmployeeRepository.update(employee, false);
        assertEquals(writeResult.getAffectedCount(), 0);
        writeResult = txEmployeeRepository.update(employee, true);
        assertEquals(writeResult.getAffectedCount(), 1);

        txEmployeeRepository.commit();
        Employee emp = employeeRepository.find(where("empId").eq(12L)).firstOrNull();
        assertEquals(emp, employee);
    }

    @Test
    public void testRemoveObject() {
        Employee employee = new Employee();
        employee.setCompany(null);
        employee.setAddress("some road");
        employee.setBlob(new byte[]{1, 2, 125});
        employee.setEmpId(12L);
        employee.setJoinDate(new Date());
        Note empNote = new Note();
        empNote.setNoteId(23L);
        empNote.setText("sample text note");
        employee.setEmployeeNote(empNote);

        long size = txEmployeeRepository.size();

        txEmployeeRepository.insert(employee);
        assertEquals(txEmployeeRepository.size(), size + 1);

        txEmployeeRepository.remove(employee);
        assertEquals(txEmployeeRepository.size(), size);

        txEmployeeRepository.commit();
        Employee emp = employeeRepository.find(where("empId").eq(12L)).firstOrNull();
        assertNull(emp);
    }

    private void prepareUpdateWithOptions(Date joiningDate) {
        txEmployeeRepository.remove(Filter.ALL);

        Employee employee1 = new Employee();
        employee1.setCompany(null);
        employee1.setAddress("some road");
        employee1.setBlob(new byte[]{1, 2, 125});
        employee1.setEmpId(12L);
        employee1.setJoinDate(joiningDate);
        Note empNote1 = new Note();
        empNote1.setNoteId(23L);
        empNote1.setText("sample text note");
        employee1.setEmployeeNote(empNote1);

        Employee employee2 = new Employee();
        employee2.setCompany(null);
        employee2.setAddress("other road");
        employee2.setBlob(new byte[]{10, 12, 25});
        employee2.setEmpId(2L);
        employee2.setJoinDate(joiningDate);
        Note empNote2 = new Note();
        empNote2.setNoteId(2L);
        empNote2.setText("some random note");
        employee2.setEmployeeNote(empNote2);

        txEmployeeRepository.insert(employee1, employee2);
        Cursor<Employee> result = txEmployeeRepository.find();
        assertEquals(result.size(), 2);
        for (Employee e : result.project(Employee.class)) {
            assertEquals(e.getJoinDate(), joiningDate);
        }
    }

    @Test
    public void testUpdateWithDoc() {
        Note note = new Note();
        note.setNoteId(10L);
        note.setText("some note text");

        Document document = createDocument("address", "some address")
            .put("employeeNote", note);

        WriteResult result = txEmployeeRepository.update(Filter.ALL, document);
        assertEquals(result.getAffectedCount(), 10);
    }

    @Test
    public void testDeleteIteratorNPE() {
        TransactionalRepository<Note> notes = db.getRepository(Note.class).beginTransaction();
        Note one = new Note();
        one.setText("Jane");
        one.setNoteId(1L);
        Note two = new Note();
        two.setText("Jill");
        two.setNoteId(2L);

        notes.insert(one, two);

        WriteResult writeResult = notes.remove(where("text").eq("Pete"));
        for (NitriteId id : writeResult) {
            assertNotNull(id);
        }
    }

    @Test
    public void testDelete() {
        TransactionalRepository<WithNitriteId> repo = db.getRepository(WithNitriteId.class).beginTransaction();
        WithNitriteId one = new WithNitriteId();
        one.setName("Jane");
        repo.insert(one);

        WithNitriteId note = repo.find().firstOrNull();
        repo.remove(note);

        assertNull(repo.getById(one.idField));
    }

    /*
     * Upsert Use Cases
     *
     * 1. Object does not exists
     *      a. if upsert true, it will insert
     *      b. if upsert false, nothing happens
     * 2. Object exists
     *      a. if upsert true, it will update, old id remains same
     *      b. if upsert false, it will update, old id remains same
     *
     * */

    @Test
    public void testUpdateObjectNotExistsUpsertTrue() {
        TransactionalRepository<InternalClass> repo = db.getRepository(InternalClass.class).beginTransaction();
        InternalClass a = new InternalClass();
        a.setId(1);
        a.setName("first");
        repo.insert(a);

        a = new InternalClass();
        a.setId(2);
        a.setName("second");

        // it will insert as new object
        repo.update(a, true);
        assertEquals(repo.find().size(), 2);
    }

    @Test
    public void testUpdateObjectNotExistsUpsertFalse() {
        TransactionalRepository<InternalClass> repo = db.getRepository(InternalClass.class).beginTransaction();
        InternalClass a = new InternalClass();
        a.setId(1);
        a.setName("first");
        repo.insert(a);

        a = new InternalClass();
        a.setId(2);
        a.setName("second");

        // no changes will happen to repository
        repo.update(a, false);
        assertEquals(repo.size(), 1);
        assertEquals(repo.find().firstOrNull().getId(), 1);
        assertEquals(repo.find().firstOrNull().getName(), "first");
    }

    @Test
    public void testUpdateObjectExistsUpsertTrue() {
        TransactionalRepository<InternalClass> repo = db.getRepository(InternalClass.class).beginTransaction();
        InternalClass a = new InternalClass();
        a.setId(1);
        a.setName("first");
        repo.insert(a);

        a = new InternalClass();
        a.setId(1);
        a.setName("second");

        // update existing object, keep id same
        repo.update(a, true);
        assertEquals(repo.size(), 1);
        assertEquals(repo.find().firstOrNull().getId(), 1);
        assertEquals(repo.find().firstOrNull().getName(), "second");
    }

    @Test
    public void testUpdateObjectExistsUpsertFalse() {
        TransactionalRepository<InternalClass> repo = db.getRepository(InternalClass.class).beginTransaction();
        InternalClass a = new InternalClass();
        a.setId(1);
        a.setName("first");
        repo.insert(a);

        a = new InternalClass();
        a.setId(1);
        a.setName("second");

        // update existing object, keep id same
        repo.update(a, false);
        assertEquals(repo.size(), 1);
        assertEquals(repo.find().firstOrNull().getId(), 1);
        assertEquals(repo.find().firstOrNull().getName(), "second");
    }
}
