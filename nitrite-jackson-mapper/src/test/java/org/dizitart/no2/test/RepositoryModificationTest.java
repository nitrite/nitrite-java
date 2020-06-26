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

package org.dizitart.no2.test;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.Cursor;
import org.dizitart.no2.test.data.Company;
import org.dizitart.no2.test.data.DataGenerator;
import org.dizitart.no2.test.data.Employee;
import org.dizitart.no2.test.data.Note;
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
public class RepositoryModificationTest extends BaseObjectRepositoryTest {
    @Test
    public void testCreateIndex() {
        assertTrue(companyRepository.hasIndex("companyName"));
        assertFalse(companyRepository.hasIndex("dateCreated"));

        companyRepository.createIndex("dateCreated", IndexOptions.indexOptions(IndexType.NonUnique));
        assertTrue(companyRepository.hasIndex("dateCreated"));
        assertFalse(companyRepository.isIndexing("dateCreated"));
    }

    @Test
    public void testRebuildIndex() {
        companyRepository.createIndex("dateCreated", IndexOptions.indexOptions(IndexType.NonUnique));
        assertFalse(companyRepository.isIndexing("dateCreated"));

        companyRepository.rebuildIndex("dateCreated", true);
        assertTrue(companyRepository.isIndexing("dateCreated"));

        await().until(() -> !companyRepository.isIndexing("dateCreated"));
    }

    @Test
    public void testListIndexes() {
        Collection<IndexEntry> indices = companyRepository.listIndices();
        assertEquals(indices.size(), 2);

        companyRepository.createIndex("dateCreated", IndexOptions.indexOptions(IndexType.NonUnique));
        indices = companyRepository.listIndices();
        assertEquals(indices.size(), 3);
    }

    @Test
    public void testDropIndex() {
        testListIndexes();
        companyRepository.dropIndex("dateCreated");
        Collection<IndexEntry> indices = companyRepository.listIndices();
        assertEquals(indices.size(), 2);
    }

    @Test
    public void testDropAllIndex() {
        testListIndexes();
        companyRepository.dropAllIndices();
        Collection<IndexEntry> indices = companyRepository.listIndices();
        assertEquals(indices.size(), 0);
    }

    @Test
    public void testCompanyRecord() {
        Cursor<Company> cursor = companyRepository.find();
        assertEquals(cursor.size(), 10);
        assertFalse(cursor.isEmpty());
    }

    @Test
    public void testInsert() {
        Company company = DataGenerator.generateCompanyRecord();
        Cursor<Company> cursor = companyRepository.find();
        assertEquals(cursor.size(), 10);

        companyRepository.insert(company);
        cursor = companyRepository.find();
        assertEquals(cursor.size(), 11);

        Company company1 = DataGenerator.generateCompanyRecord();
        Company company2 = DataGenerator.generateCompanyRecord();
        companyRepository.insert(new Company[]{company1, company2});
        cursor = companyRepository.find();
        assertEquals(cursor.size(), 13);
    }

    @Test
    public void testUpdateWithFilter() {
        employeeRepository.remove(Filter.ALL);

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

        employeeRepository.insert(employee);
        Cursor<Employee> result = employeeRepository.find();
        assertEquals(result.size(), 1);
        for (Employee e : result) {
            assertEquals(e.getAddress(), "abcd road");
        }

        Employee updated = new Employee(employee);
        updated.setAddress("xyz road");
        WriteResult writeResult = employeeRepository.update(where("empId").eq(12L), updated);
        assertEquals(writeResult.getAffectedCount(), 1);
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
            = employeeRepository.update(where("empId").eq(12L), updated1, false);
        assertEquals(writeResult.getAffectedCount(), 1);

        Cursor<Employee> result = employeeRepository.find(where("joinDate").eq(joiningDate));
        assertEquals(result.size(), 1);
        result = employeeRepository.find(where("joinDate").eq(newJoiningDate));
        assertEquals(result.size(), 1);

        employeeRepository.remove(Filter.ALL);
        prepareUpdateWithOptions(joiningDate);
        result = employeeRepository.find();
        assertEquals(result.size(), 2);

        Document update = createDocument();
        update.put("joinDate", newJoiningDate);

        writeResult = employeeRepository.update(where("joinDate").eq(joiningDate), update, false);
        assertEquals(writeResult.getAffectedCount(), 2);

        result = employeeRepository.find(where("joinDate").eq(joiningDate));
        assertEquals(result.size(), 0);

        result = employeeRepository.find(where("joinDate").eq(newJoiningDate));
        assertEquals(result.size(), 2);
    }

    @Test
    public void testUpsertTrue() {
        Date joiningDate = new Date();
        Cursor result = employeeRepository.find(where("joinDate").eq(joiningDate));
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
            = employeeRepository.update(where("empId").eq(12), employee, true);
        assertEquals(writeResult.getAffectedCount(), 1);

        result = employeeRepository.find(where("joinDate").eq(joiningDate));
        assertEquals(result.size(), 1);
    }

    @Test
    public void testUpsertFalse() {
        Date joiningDate = new Date();
        Cursor result = employeeRepository.find(where("joinDate").eq(joiningDate));
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
            = employeeRepository.update(where("empId").eq(12), employee, false);
        assertEquals(writeResult.getAffectedCount(), 0);

        result = employeeRepository.find(where("joinDate").eq(joiningDate));
        assertEquals(result.size(), 0);
    }

    @Test
    public void testDeleteFilterAndWithOutOption() {
        Date joiningDate = new Date();
        prepareUpdateWithOptions(joiningDate);

        Cursor result = employeeRepository.find(where("joinDate").eq(joiningDate));
        assertEquals(result.size(), 2);

        WriteResult writeResult = employeeRepository.remove(where("joinDate").eq(joiningDate));
        assertEquals(writeResult.getAffectedCount(), 2);
        result = employeeRepository.find(where("joinDate").eq(joiningDate));
        assertEquals(result.size(), 0);
    }

    @Test
    public void testDeleteFilterAndWithOption() {
        Date joiningDate = new Date();
        prepareUpdateWithOptions(joiningDate);

        Cursor result = employeeRepository.find(where("joinDate").eq(joiningDate));
        assertEquals(result.size(), 2);

        WriteResult writeResult = employeeRepository.remove(where("joinDate").eq(joiningDate), true);
        assertEquals(writeResult.getAffectedCount(), 1);
        result = employeeRepository.find(where("joinDate").eq(joiningDate));
        assertEquals(result.size(), 1);
    }

    @Test
    public void testEmployeeRecord() {
        Iterable<Employee> totalResult = employeeRepository.find();
        int occurrence = 0;
        for (Employee employee : totalResult) {
            if (employee.getEmployeeNote().getText().toLowerCase().contains("class aptent")) {
                occurrence++;
            }
        }

        Cursor cursor = employeeRepository.find(where("employeeNote.text").text("Class aptent"));
        assertEquals(cursor.size(), occurrence);
    }

    @Test
    public void testUpdateWithOptions() {
        Employee employee = employeeRepository.find().firstOrNull();

        Document update = createDocument();
        update.put("address", "new address");

        WriteResult writeResult
            = employeeRepository.update(where("empId").eq(employee.getEmpId()), update, false);
        assertEquals(writeResult.getAffectedCount(), 1);

        Employee byId = employeeRepository.getById(employee.getEmpId());
        assertEquals(byId.getAddress(), "new address");
        assertEquals(byId.getEmpId(), employee.getEmpId());

        update.put("address", "another address");
        employeeRepository.update(where("empId").eq(employee.getEmpId()), update);

        byId = employeeRepository.getById(employee.getEmpId());
        assertEquals(byId.getAddress(), "another address");
        assertEquals(byId.getEmpId(), employee.getEmpId());
    }

    @Test(expected = InvalidIdException.class)
    public void testMultiUpdateWithObject() {
        employeeRepository.remove(Filter.ALL);

        Date now = new Date();
        Employee employee1 = new Employee();
        employee1.setEmpId(1L);
        employee1.setAddress("abcd");
        employee1.setJoinDate(now);

        Employee employee2 = new Employee();
        employee2.setEmpId(2L);
        employee2.setAddress("xyz");
        employee2.setJoinDate(now);
        employeeRepository.insert(employee1, employee2);

        Employee update = new Employee();
        update.setAddress("new address");

        WriteResult writeResult
            = employeeRepository.update(where("joinDate").eq(now), update, false);
        assertEquals(writeResult.getAffectedCount(), 0);
    }

    @Test
    public void testUpdateNull() {
        Employee employee = employeeRepository.find().firstOrNull();
        Employee newEmployee = new Employee(employee);
        newEmployee.setJoinDate(null);

        Employee result = employeeRepository.find(where("empId").eq(employee.getEmpId())).firstOrNull();
        assertNotNull(result.getJoinDate());

        WriteResult writeResult = employeeRepository.update(newEmployee, false);
        assertEquals(writeResult.getAffectedCount(), 1);

        result = employeeRepository.find(where("empId").eq(employee.getEmpId())).firstOrNull();
        assertNull(result.getJoinDate());

        // update with object filter and item and set id different
    }

    @Test
    public void testUpdateWithChangedId() {
        Employee employee = employeeRepository.find().firstOrNull();
        Long oldId = employee.getEmpId();
        long count = employeeRepository.size();

        Employee newEmployee = new Employee(employee);
        newEmployee.setEmpId(50L);

        Employee result = employeeRepository.find(where("empId").eq(oldId)).firstOrNull();
        assertNotNull(result.getJoinDate());

        WriteResult writeResult = employeeRepository.update(where("empId").eq(oldId), newEmployee, false);
        assertEquals(writeResult.getAffectedCount(), 1);

        assertEquals(count, employeeRepository.size());
        Cursor<Employee> cursor = employeeRepository.find(where("empId").eq(oldId));
        assertEquals(cursor.size(), 0);
    }

    @Test(expected = InvalidIdException.class)
    public void testUpdateWithNullId() {
        Employee employee = employeeRepository.find().firstOrNull();
        Long oldId = employee.getEmpId();

        Employee newEmployee = new Employee(employee);
        newEmployee.setEmpId(null);

        Employee result = employeeRepository.find(where("empId").eq(oldId)).firstOrNull();
        assertNotNull(result.getJoinDate());

        WriteResult writeResult = employeeRepository.update(where("empId").eq(oldId), newEmployee, false);
        assertEquals(writeResult.getAffectedCount(), 1);
    }

    @Test(expected = UniqueConstraintException.class)
    public void testUpdateWithDuplicateId() {
        Employee employee = employeeRepository.find().firstOrNull();
        Long oldId = employee.getEmpId();
        long count = employeeRepository.size();

        Employee newEmployee = new Employee(employee);
        newEmployee.setEmpId(5L);

        Employee result = employeeRepository.find(where("empId").eq(oldId)).firstOrNull();
        assertNotNull(result.getJoinDate());

        WriteResult writeResult = employeeRepository.update(where("empId").eq(oldId), newEmployee, false);
        assertEquals(writeResult.getAffectedCount(), 1);

        assertEquals(count, employeeRepository.size());
        Cursor<Employee> cursor = employeeRepository.find(where("empId").eq(oldId));
        assertEquals(cursor.size(), 0);
    }

    @Test
    public void testUpdateWithObject() {
        Employee employee = employeeRepository.find().firstOrNull();
        Employee newEmployee = new Employee(employee);

        Long id = employee.getEmpId();
        String address = employee.getAddress();
        newEmployee.setAddress("new address");

        WriteResult writeResult = employeeRepository.update(newEmployee);
        assertEquals(writeResult.getAffectedCount(), 1);

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

        WriteResult writeResult = employeeRepository.update(employee, false);
        assertEquals(writeResult.getAffectedCount(), 0);
        writeResult = employeeRepository.update(employee, true);
        assertEquals(writeResult.getAffectedCount(), 1);

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

        long size = employeeRepository.size();

        employeeRepository.insert(employee);
        assertEquals(employeeRepository.size(), size + 1);

        employeeRepository.remove(employee);
        assertEquals(employeeRepository.size(), size);

        Employee emp = employeeRepository.find(where("empId").eq(12L)).firstOrNull();
        assertNull(emp);
    }

    private void prepareUpdateWithOptions(Date joiningDate) {
        employeeRepository.remove(Filter.ALL);

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

        employeeRepository.insert(employee1, employee2);
        Cursor<Employee> result = employeeRepository.find();
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

        WriteResult result = employeeRepository.update(Filter.ALL, document);
        assertEquals(result.getAffectedCount(), 10);
    }
}
