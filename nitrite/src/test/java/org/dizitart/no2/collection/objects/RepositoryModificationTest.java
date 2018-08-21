/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.collection.objects;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.IndexOptions;
import org.dizitart.no2.collection.IndexType;
import org.dizitart.no2.collection.RemoveOptions;
import org.dizitart.no2.collection.WriteResult;
import org.dizitart.no2.collection.objects.data.Company;
import org.dizitart.no2.collection.objects.data.DataGenerator;
import org.dizitart.no2.collection.objects.data.Employee;
import org.dizitart.no2.collection.objects.data.Note;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.filters.ObjectFilters;
import org.dizitart.no2.index.Index;
import org.dizitart.no2.util.Iterables;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import static org.awaitility.Awaitility.await;
import static org.dizitart.no2.filters.ObjectFilters.*;
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
        Collection<Index> indices = companyRepository.listIndices();
        assertEquals(indices.size(), 2);

        companyRepository.createIndex("dateCreated", IndexOptions.indexOptions(IndexType.NonUnique));
        indices = companyRepository.listIndices();
        assertEquals(indices.size(), 3);
    }

    @Test
    public void testDropIndex() {
        testListIndexes();
        companyRepository.dropIndex("dateCreated");
        Collection<Index> indices = companyRepository.listIndices();
        assertEquals(indices.size(), 2);
    }

    @Test
    public void testDropAllIndex() {
        testListIndexes();
        companyRepository.dropAllIndices();
        Collection<Index> indices = companyRepository.listIndices();
        assertEquals(indices.size(), 0);
    }

    @Test
    public void testCompanyRecord() {
        Cursor cursor = companyRepository.find();
        assertEquals(cursor.size(), 10);
        assertFalse(cursor.hasMore());
    }

    @Test
    public void testInsert() {
        Company company = DataGenerator.generateCompanyRecord();
        Cursor cursor = companyRepository.find();
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
        employeeRepository.remove(ALL);

        Employee employee = new Employee();
        employee.setCompany(null);
        employee.setAddress("abcd road");
        employee.setBlob(new byte[] {1, 2, 125});
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
        WriteResult writeResult = employeeRepository.update(eq("empId", 12L), updated);
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

        Document updated1 = new Document();
        updated1.put("joinDate", newJoiningDate);

        WriteResult writeResult
                = employeeRepository.update(eq("empId", 12L), updated1, false);
        assertEquals(writeResult.getAffectedCount(), 1);

        Cursor<Employee> result = employeeRepository.find(eq("joinDate", joiningDate));
        assertEquals(result.size(), 1);
        result = employeeRepository.find(eq("joinDate", newJoiningDate));
        assertEquals(result.size(), 1);

        employeeRepository.remove(ALL);
        prepareUpdateWithOptions(joiningDate);
        result = employeeRepository.find();
        assertEquals(result.size(), 2);

        Document update = new Document();
        update.put("joinDate", newJoiningDate);

        writeResult = employeeRepository.update(eq("joinDate", joiningDate), update, false);
        assertEquals(writeResult.getAffectedCount(), 2);

        result = employeeRepository.find(eq("joinDate", joiningDate));
        assertEquals(result.size(), 0);

        result = employeeRepository.find(eq("joinDate", newJoiningDate));
        assertEquals(result.size(), 2);
    }

    @Test
    public void testUpsertTrue() {
        Date joiningDate = new Date();
        Cursor result = employeeRepository.find(eq("joinDate", joiningDate));
        assertEquals(result.size(), 0);

        Employee employee = new Employee();
        employee.setCompany(null);
        employee.setAddress("some road");
        employee.setBlob(new byte[] {1, 2, 125});
        employee.setEmpId(12L);
        employee.setJoinDate(joiningDate);
        Note empNote1 = new Note();
        empNote1.setNoteId(23L);
        empNote1.setText("sample text note");
        employee.setEmployeeNote(empNote1);

        WriteResult writeResult
                = employeeRepository.update(eq("empId", 12), employee, true);
        assertEquals(writeResult.getAffectedCount(), 1);

        result = employeeRepository.find(eq("joinDate", joiningDate));
        assertEquals(result.size(), 1);
    }

    @Test
    public void testUpsertFalse() {
        Date joiningDate = new Date();
        Cursor result = employeeRepository.find(eq("joinDate", joiningDate));
        assertEquals(result.size(), 0);

        Employee employee = new Employee();
        employee.setCompany(null);
        employee.setAddress("some road");
        employee.setBlob(new byte[] {1, 2, 125});
        employee.setEmpId(12L);
        employee.setJoinDate(joiningDate);
        Note empNote1 = new Note();
        empNote1.setNoteId(23L);
        empNote1.setText("sample text note");
        employee.setEmployeeNote(empNote1);

        WriteResult writeResult
                = employeeRepository.update(eq("empId", 12), employee, false);
        assertEquals(writeResult.getAffectedCount(), 0);

        result = employeeRepository.find(eq("joinDate", joiningDate));
        assertEquals(result.size(), 0);
    }

    @Test
    public void testDeleteFilterAndWithOutOption() {
        Date joiningDate = new Date();
        prepareUpdateWithOptions(joiningDate);

        Cursor result = employeeRepository.find(eq("joinDate", joiningDate));
        assertEquals(result.size(), 2);

        WriteResult writeResult = employeeRepository.remove(eq("joinDate", joiningDate));
        assertEquals(writeResult.getAffectedCount(), 2);
        result = employeeRepository.find(eq("joinDate", joiningDate));
        assertEquals(result.size(), 0);
    }

    @Test
    public void testDeleteFilterAndWithOption() {
        Date joiningDate = new Date();
        prepareUpdateWithOptions(joiningDate);

        Cursor result = employeeRepository.find(eq("joinDate", joiningDate));
        assertEquals(result.size(), 2);

        RemoveOptions removeOptions = new RemoveOptions();
        removeOptions.setJustOne(true);
        WriteResult writeResult = employeeRepository.remove(eq("joinDate", joiningDate), removeOptions);
        assertEquals(writeResult.getAffectedCount(), 1);
        result = employeeRepository.find(eq("joinDate", joiningDate));
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

        Cursor cursor = employeeRepository.find(text("employeeNote.text", "Class aptent"));
        assertEquals(cursor.size(), occurrence);
    }

    @Test
    public void testUpdateWithOptions() {
        Employee employee = employeeRepository.find().firstOrDefault();

        Document update = new Document();
        update.put("address", "new address");

        WriteResult writeResult
                = employeeRepository.update(eq("empId", employee.getEmpId()), update, false);
        assertEquals(writeResult.getAffectedCount(), 1);

        NitriteId nitriteId = Iterables.firstOrDefault(writeResult);
        Employee byId = employeeRepository.getById(nitriteId);
        assertEquals(byId.getAddress(), "new address");
        assertEquals(byId.getEmpId(), employee.getEmpId());

        update.put("address", "another address");
        writeResult
                = employeeRepository.update(eq("empId", employee.getEmpId()), update);
        nitriteId = Iterables.firstOrDefault(writeResult);
        byId = employeeRepository.getById(nitriteId);
        assertEquals(byId.getAddress(), "another address");
        assertEquals(byId.getEmpId(), employee.getEmpId());
    }

    @Test(expected = InvalidIdException.class)
    public void testMultiUpdateWithObject() {
        employeeRepository.remove(ObjectFilters.ALL);

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
            = employeeRepository.update(eq("joinDate", now), update, false);
        assertEquals(writeResult.getAffectedCount(), 0);
    }

    @Test
    public void testUpdateNull() {
        Employee employee = employeeRepository.find().firstOrDefault();
        Employee newEmployee = new Employee(employee);
        newEmployee.setJoinDate(null);

        Employee result = employeeRepository.find(eq("empId", employee.getEmpId())).firstOrDefault();
        assertNotNull(result.getJoinDate());

        WriteResult writeResult = employeeRepository.update(newEmployee, false);
        assertEquals(writeResult.getAffectedCount(), 1);

        result = employeeRepository.find(eq("empId", employee.getEmpId())).firstOrDefault();
        assertNull(result.getJoinDate());

        // update with object filter and item and set id different
    }

    @Test
    public void testUpdateWithChangedId() {
        Employee employee = employeeRepository.find().firstOrDefault();
        Long oldId = employee.getEmpId();
        long count = employeeRepository.size();

        Employee newEmployee = new Employee(employee);
        newEmployee.setEmpId(50L);

        Employee result = employeeRepository.find(eq("empId", oldId)).firstOrDefault();
        assertNotNull(result.getJoinDate());

        WriteResult writeResult = employeeRepository.update(eq("empId", oldId), newEmployee, false);
        assertEquals(writeResult.getAffectedCount(), 1);

        assertEquals(count, employeeRepository.size());
        Cursor<Employee> cursor = employeeRepository.find(eq("empId", oldId));
        assertEquals(cursor.size(), 0);
    }

    @Test(expected = InvalidIdException.class)
    public void testUpdateWithNullId() {
        Employee employee = employeeRepository.find().firstOrDefault();
        Long oldId = employee.getEmpId();

        Employee newEmployee = new Employee(employee);
        newEmployee.setEmpId(null);

        Employee result = employeeRepository.find(eq("empId", oldId)).firstOrDefault();
        assertNotNull(result.getJoinDate());

        WriteResult writeResult = employeeRepository.update(eq("empId", oldId), newEmployee, false);
        assertEquals(writeResult.getAffectedCount(), 1);
    }

    @Test(expected = UniqueConstraintException.class)
    public void testUpdateWithDuplicateId() {
        Employee employee = employeeRepository.find().firstOrDefault();
        Long oldId = employee.getEmpId();
        long count = employeeRepository.size();

        Employee newEmployee = new Employee(employee);
        newEmployee.setEmpId(5L);

        Employee result = employeeRepository.find(eq("empId", oldId)).firstOrDefault();
        assertNotNull(result.getJoinDate());

        WriteResult writeResult = employeeRepository.update(eq("empId", oldId), newEmployee, false);
        assertEquals(writeResult.getAffectedCount(), 1);

        assertEquals(count, employeeRepository.size());
        Cursor<Employee> cursor = employeeRepository.find(eq("empId", oldId));
        assertEquals(cursor.size(), 0);
    }

    @Test
    public void testUpdateWithObject() {
        Employee employee = employeeRepository.find().firstOrDefault();
        Employee newEmployee = new Employee(employee);

        Long id = employee.getEmpId();
        String address = employee.getAddress();
        newEmployee.setAddress("new address");

        WriteResult writeResult = employeeRepository.update(newEmployee);
        assertEquals(writeResult.getAffectedCount(), 1);

        Employee emp = employeeRepository.find(eq("empId", id)).firstOrDefault();
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
        employee.setBlob(new byte[] {1, 2, 125});
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

        Employee emp = employeeRepository.find(eq("empId", 12L)).firstOrDefault();
        assertEquals(emp, employee);
    }

    @Test
    public void testRemoveObject() {
        Employee employee = new Employee();
        employee.setCompany(null);
        employee.setAddress("some road");
        employee.setBlob(new byte[] {1, 2, 125});
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

        Employee emp = employeeRepository.find(eq("empId", 12L)).firstOrDefault();
        assertNull(emp);
    }

    private void prepareUpdateWithOptions(Date joiningDate) {
        employeeRepository.remove(ALL);

        Employee employee1 = new Employee();
        employee1.setCompany(null);
        employee1.setAddress("some road");
        employee1.setBlob(new byte[] {1, 2, 125});
        employee1.setEmpId(12L);
        employee1.setJoinDate(joiningDate);
        Note empNote1 = new Note();
        empNote1.setNoteId(23L);
        empNote1.setText("sample text note");
        employee1.setEmployeeNote(empNote1);

        Employee employee2 = new Employee();
        employee2.setCompany(null);
        employee2.setAddress("other road");
        employee2.setBlob(new byte[] {10, 12, 25});
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
}
