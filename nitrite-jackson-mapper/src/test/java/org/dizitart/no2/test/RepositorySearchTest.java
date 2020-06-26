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

import lombok.Getter;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.exceptions.NotIdentifiableException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.repository.Cursor;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.test.data.*;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.dizitart.no2.filters.Filter.ALL;
import static org.dizitart.no2.filters.FluentFilter.$;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
public class RepositorySearchTest extends BaseObjectRepositoryTest {
    @Test
    public void testFindWithOptions() {
        Cursor<Employee> cursor = employeeRepository.find().limit(0, 1);
        assertEquals(cursor.size(), 1);
        assertNotNull(cursor.firstOrNull());
    }

    @Test
    public void testEmployeeProjection() {
        List<Employee> employeeList = employeeRepository.find().toList();
        List<SubEmployee> subEmployeeList
            = employeeRepository.find().project(SubEmployee.class).toList();

        assertNotNull(employeeList);
        assertNotNull(subEmployeeList);

        assertTrue(employeeList.size() > 0);
        assertTrue(subEmployeeList.size() > 0);

        assertEquals(employeeList.size(), subEmployeeList.size());

        for (int i = 0; i < subEmployeeList.size(); i++) {
            Employee employee = employeeList.get(i);
            SubEmployee subEmployee = subEmployeeList.get(i);

            assertEquals(employee.getEmpId(), subEmployee.getEmpId());
            assertEquals(employee.getJoinDate(), subEmployee.getJoinDate());
            assertEquals(employee.getAddress(), subEmployee.getAddress());
        }

        Cursor<Employee> cursor = employeeRepository.find();
        assertNotNull(cursor.firstOrNull());
        assertNotNull(cursor.toString());
        assertEquals(cursor.toList().size(), employeeList.size());
        assertNotNull(cursor.firstOrNull());
        assertEquals(cursor.toList().size(), employeeList.size());
    }

    @Test
    public void testEmptyResultProjection() {
        employeeRepository.remove(ALL);
        assertNull(employeeRepository.find().firstOrNull());

        assertNull(employeeRepository.find(where("empId").eq(-1))
            .firstOrNull());
    }

    @Test
    public void testGetById() {
        ObjectRepository<Employee> empRepo = db.getRepository(Employee.class);
        Employee e1 = DataGenerator.generateEmployee();
        Employee e2 = DataGenerator.generateEmployee();
        Employee e3 = DataGenerator.generateEmployee();
        Employee e4 = DataGenerator.generateEmployee();

        e1.setEmpId(1000000L);
        e2.setEmpId(2000000L);
        e3.setEmpId(3000000L);
        e4.setEmpId(4000000L);

        empRepo.insert(e1, e2, e3, e4);

        Employee byId = empRepo.getById(2000000L);
        assertEquals(byId, e2);
    }

    @Test(expected = NotIdentifiableException.class)
    public void testGetByIdNoId() {
        ObjectRepository<Note> repository = db.getRepository(Note.class);
        Note n1 = DataGenerator.randomNote();
        Note n2 = DataGenerator.randomNote();
        Note n3 = DataGenerator.randomNote();

        assert n1 != null;
        n1.setNoteId(1000000L);
        assert n2 != null;
        n2.setNoteId(2000000L);
        assert n3 != null;
        n3.setNoteId(3000000L);

        repository.insert(n1, n2, n3);

        repository.getById(2000000L);
    }

    @Test(expected = InvalidIdException.class)
    public void testGetByIdNullId() {
        ObjectRepository<Employee> empRepo = db.getRepository(Employee.class);
        Employee e1 = DataGenerator.generateEmployee();
        Employee e2 = DataGenerator.generateEmployee();
        Employee e3 = DataGenerator.generateEmployee();
        Employee e4 = DataGenerator.generateEmployee();

        e1.setEmpId(1000000L);
        e2.setEmpId(2000000L);
        e3.setEmpId(3000000L);
        e4.setEmpId(4000000L);

        empRepo.insert(e1, e2, e3, e4);

        empRepo.getById(null);
    }

    @Test(expected = InvalidIdException.class)
    public void testGetByIdWrongType() {
        ObjectRepository<Employee> empRepo = db.getRepository(Employee.class);
        Employee e1 = DataGenerator.generateEmployee();
        Employee e2 = DataGenerator.generateEmployee();
        Employee e3 = DataGenerator.generateEmployee();
        Employee e4 = DataGenerator.generateEmployee();

        e1.setEmpId(1000000L);
        e2.setEmpId(2000000L);
        e3.setEmpId(3000000L);
        e4.setEmpId(4000000L);

        empRepo.insert(e1, e2, e3, e4);

        Employee byId = empRepo.getById("employee");
        assertNull(byId);
    }

    @Test
    public void testEqualFilterById() {
        Employee employee = employeeRepository.find().firstOrNull();
        long empId = employee.getEmpId();
        Employee emp = employeeRepository.find(where("empId").eq(empId))
            .project(Employee.class).firstOrNull();
        assertEquals(employee, emp);
    }

    @Test
    public void testEqualFilter() {
        Employee employee = employeeRepository.find()
            .firstOrNull();

        Employee emp = employeeRepository.find(where("joinDate").eq(employee.getJoinDate()))
            .project(Employee.class)
            .firstOrNull();
        assertEquals(employee, emp);
    }

    @Test
    public void testStringEqualFilter() {
        ObjectRepository<ProductScore> repository = db.getRepository(ProductScore.class);

        ProductScore object = new ProductScore();
        object.setProduct("test");
        object.setScore(1);
        repository.insert(object);

        object = new ProductScore();
        object.setProduct("test");
        object.setScore(2);
        repository.insert(object);

        object = new ProductScore();
        object.setProduct("another-test");
        object.setScore(3);
        repository.insert(object);

        assertEquals(repository.find(where("product").eq("test")).size(), 2);
    }

    @Test
    public void testAndFilter() {
        Employee emp = employeeRepository.find().firstOrNull();

        long id = emp.getEmpId();
        String address = emp.getAddress();
        Date joinDate = emp.getJoinDate();

        Employee employee = employeeRepository.find(
            where("empId").eq(id)
                .and(
                    where("address").regex(address)
                        .and(
                            where("joinDate").eq(joinDate)))).firstOrNull();

        assertEquals(emp, employee);
    }

    @Test
    public void testOrFilter() {
        Employee emp = employeeRepository.find().firstOrNull();
        long id = emp.getEmpId();

        Employee employee = employeeRepository.find(
            where("empId").eq(id)
                .or(
                    where("address").regex("n/a")
                        .or(
                            where("joinDate").eq(null)))).firstOrNull();

        assertEquals(emp, employee);
    }

    @Test
    public void testNotFilter() {
        Employee emp = employeeRepository.find().firstOrNull();
        long id = emp.getEmpId();

        Employee employee = employeeRepository.find(
            where("empId").eq(id).not()).firstOrNull();
        assertNotEquals(emp, employee);
    }

    @Test
    public void testGreaterFilter() {
        Employee emp = employeeRepository.find().sort("empId", SortOrder.Ascending).firstOrNull();
        long id = emp.getEmpId();

        List<Employee> employeeList = employeeRepository.find(where("empId").gt(id))
            .toList();

        assertFalse(employeeList.contains(emp));
        assertEquals(employeeList.size(), 9);
    }

    @Test
    public void testGreaterEqualFilter() {
        Employee emp = employeeRepository.find().sort("empId", SortOrder.Ascending).firstOrNull();
        long id = emp.getEmpId();

        List<Employee> employeeList = employeeRepository.find(where("empId").gte(id))
            .toList();

        assertTrue(employeeList.contains(emp));
        assertEquals(employeeList.size(), 10);
    }

    @Test
    public void testLesserThanFilter() {
        Employee emp = employeeRepository.find().sort("empId", SortOrder.Descending).firstOrNull();
        long id = emp.getEmpId();

        List<Employee> employeeList = employeeRepository.find(where("empId").lt(id))
            .toList();

        assertFalse(employeeList.contains(emp));
        assertEquals(employeeList.size(), 9);
    }

    @Test
    public void testLesserEqualFilter() {
        Employee emp = employeeRepository.find().sort("empId", SortOrder.Descending).firstOrNull();
        long id = emp.getEmpId();

        List<Employee> employeeList = employeeRepository.find(where("empId").lte(id))
            .toList();

        assertTrue(employeeList.contains(emp));
        assertEquals(employeeList.size(), 10);
    }

    @Test
    public void testTextFilter() {
        Employee emp = employeeRepository.find().firstOrNull();
        String text = emp.getEmployeeNote().getText();

        List<Employee> employeeList = employeeRepository.find(where("employeeNote.text").text(text))
            .toList();

        assertTrue(employeeList.contains(emp));
    }

    @Test
    public void testRegexFilter() {
        ReadableStream<Employee> employees = employeeRepository.find();
        int count = employees.toList().size();

        List<Employee> employeeList = employeeRepository.find(where("employeeNote.text").regex(".*"))
            .toList();

        assertEquals(employeeList.size(), count);
    }

    @Test
    public void testInFilter() {
        Employee emp = employeeRepository.find().sort("empId", SortOrder.Descending).firstOrNull();
        long id = emp.getEmpId();

        List<Employee> employeeList = employeeRepository.find(where("empId").in(id, id - 1, id - 2))
            .toList();

        assertTrue(employeeList.contains(emp));
        assertEquals(employeeList.size(), 3);

        employeeList = employeeRepository.find(where("empId").in(id - 1, id - 2)).toList();
        assertEquals(employeeList.size(), 2);
    }

    @Test
    public void testNotInFilter() {
        Employee emp = employeeRepository.find().sort("empId", SortOrder.Descending).firstOrNull();
        long id = emp.getEmpId();

        List<Employee> employeeList = employeeRepository.find(where("empId").notIn(id, id - 1, id - 2))
            .toList();

        assertFalse(employeeList.contains(emp));
        assertEquals(employeeList.size(), 7);

        employeeList = employeeRepository.find(where("empId").notIn(id - 1, id - 2)).toList();
        assertEquals(employeeList.size(), 8);
    }

    @Test
    public void testElemMatchFilter() {
        final ProductScore score1 = new ProductScore("abc", 10);
        final ProductScore score2 = new ProductScore("abc", 8);
        final ProductScore score3 = new ProductScore("abc", 7);
        final ProductScore score4 = new ProductScore("xyz", 5);
        final ProductScore score5 = new ProductScore("xyz", 7);
        final ProductScore score6 = new ProductScore("xyz", 8);

        ObjectRepository<ElemMatch> repository = db.getRepository(ElemMatch.class);
        ElemMatch e1 = new ElemMatch() {{
            setId(1);
            setStrArray(new String[]{"a", "b"});
            setProductScores(new ProductScore[]{score1, score4});
        }};
        ElemMatch e2 = new ElemMatch() {{
            setId(2);
            setStrArray(new String[]{"d", "e"});
            setProductScores(new ProductScore[]{score2, score5});
        }};
        ElemMatch e3 = new ElemMatch() {{
            setId(3);
            setStrArray(new String[]{"a", "f"});
            setProductScores(new ProductScore[]{score3, score6});
        }};

        repository.insert(e1, e2, e3);

        List<ElemMatch> elements = repository.find(
            where("productScores").elemMatch(
                where("product").eq("xyz")
                    .and(where("score").gte(8)))).toList();
        assertEquals(elements.size(), 1);

        elements = repository.find(
            where("productScores").elemMatch(
                where("score").lte(8).not())).toList();
        assertEquals(elements.size(), 1);

        elements = repository.find(
            where("productScores").elemMatch(
                where("product").eq("xyz")
                    .or(where("score").gte(8)))).toList();
        assertEquals(elements.size(), 3);

        elements = repository.find(
            where("productScores").elemMatch(
                where("product").eq("xyz"))).toList();
        assertEquals(elements.size(), 3);

        elements = repository.find(
            where("productScores").elemMatch(
                where("score").gte(10))).toList();
        assertEquals(elements.size(), 1);

        elements = repository.find(
            where("productScores").elemMatch(
                where("score").gt(8))).toList();
        assertEquals(elements.size(), 1);

        elements = repository.find(
            where("productScores").elemMatch(
                where("score").lt(7))).toList();
        assertEquals(elements.size(), 1);

        elements = repository.find(
            where("productScores").elemMatch(
                where("score").lte(7))).toList();
        assertEquals(elements.size(), 3);

        elements = repository.find(
            where("productScores").elemMatch(
                where("score").in(7, 8))).toList();
        assertEquals(elements.size(), 2);

        elements = repository.find(
            where("productScores").elemMatch(
                where("score").notIn(7, 8))).toList();
        assertEquals(elements.size(), 1);

        elements = repository.find(
            where("productScores").elemMatch(
                where("product").regex("xyz"))).toList();
        assertEquals(elements.size(), 3);

        elements = repository.find(where("strArray").elemMatch($.eq("a"))).toList();
        assertEquals(elements.size(), 2);

        elements = repository.find(
            where("strArray").elemMatch(
                $.eq("a")
                    .or($.eq("f")
                        .or($.eq("b"))).not())).toList();
        assertEquals(elements.size(), 1);

        elements = repository.find(where("strArray").elemMatch($.gt("e"))).toList();
        assertEquals(elements.size(), 1);

        elements = repository.find(where("strArray").elemMatch($.gte("e"))).toList();
        assertEquals(elements.size(), 2);

        elements = repository.find(where("strArray").elemMatch($.lte("b"))).toList();
        assertEquals(elements.size(), 2);

        elements = repository.find(where("strArray").elemMatch($.lt("a"))).toList();
        assertEquals(elements.size(), 0);

        elements = repository.find(where("strArray").elemMatch($.in("a", "f"))).toList();
        assertEquals(elements.size(), 2);

        elements = repository.find(where("strArray").elemMatch($.regex("a"))).toList();
        assertEquals(elements.size(), 2);
    }

    @Test
    public void testFilterAll() {
        ObjectRepository<ElemMatch> repository = db.getRepository(ElemMatch.class);
        Cursor<ElemMatch> cursor = repository.find(ALL);
        assertNotNull(cursor);
        assertEquals(cursor.size(), 0);

        repository.insert(new ElemMatch());
        cursor = repository.find(ALL);
        assertNotNull(cursor);
        assertEquals(cursor.size(), 1);
    }

    @Test
    public void testEqualsOnTextIndex() {
        PersonEntity p1 = new PersonEntity("jhonny");
        PersonEntity p2 = new PersonEntity("jhonny");
        PersonEntity p3 = new PersonEntity("jhonny");

        ObjectRepository<PersonEntity> repository = db.getRepository(PersonEntity.class);
        repository.insert(p1);
        repository.insert(p2);
        repository.insert(p3);

        List<PersonEntity> sameNamePeople = repository.find(where("name").eq("jhonny")).toList();
        assertEquals(sameNamePeople.size(), 3);

        sameNamePeople = repository.find(where("name").eq("JHONNY")).toList();
        assertEquals(sameNamePeople.size(), 0);

        sameNamePeople = repository.find(where("name").text("jhonny")).toList();
        assertEquals(sameNamePeople.size(), 3);

        sameNamePeople = repository.find(where("name").text("JHONNY")).toList();
        assertEquals(sameNamePeople.size(), 3);

        sameNamePeople = repository.find(where("name").eq("jhon*")).toList();
        assertEquals(sameNamePeople.size(), 0);

        sameNamePeople = repository.find(where("name").text("jhon*")).toList();
        assertEquals(sameNamePeople.size(), 3);
    }

    @Test
    public void testIssue62() {
        PersonEntity p1 = new PersonEntity("abcd");
        p1.setStatus("Married");

        PersonEntity p2 = new PersonEntity("efgh");
        p2.setStatus("Married");

        PersonEntity p3 = new PersonEntity("ijkl");
        p3.setStatus("Un-Married");

        ObjectRepository<PersonEntity> repository = db.getRepository(PersonEntity.class);
        repository.insert(p1);
        repository.insert(p2);
        repository.insert(p3);

        Filter married = where("status").eq("Married");

        assertEquals(repository.find(married).size(), 2);
        assertEquals(repository.find(married).sort("status", SortOrder.Descending).size(), 2);

        assertEquals(repository.find().sort("status", SortOrder.Descending).firstOrNull().getStatus(), "Un-Married");

        assertEquals(repository.find().sort("status", SortOrder.Ascending).size(), 3);
        assertEquals(repository.find().sort("status", SortOrder.Ascending).firstOrNull().getStatus(), "Married");
    }

    @Test
    public void testRepeatableIndexAnnotation() {
        ObjectRepository<RepeatableIndexTest> repo = db.getRepository(RepeatableIndexTest.class);
        RepeatableIndexTest first = new RepeatableIndexTest();
        first.setAge(12);
        first.setFirstName("fName");
        first.setLastName("lName");
        repo.insert(first);

        assertTrue(repo.hasIndex("firstName"));
        assertTrue(repo.hasIndex("age"));
        assertTrue(repo.hasIndex("lastName"));

        assertEquals(repo.find(where("age").eq(12)).firstOrNull(), first);
    }

    @Test
    public void testIdSet() {
        Cursor<Employee> employees = employeeRepository.find().sort("empId", SortOrder.Ascending);
        assertEquals(employees.size(), 10);
    }

    @Test
    public void testBetweenFilter() {
        @Getter
        class TestData {
            private final Date age;

            public TestData(Date age) {
                this.age = age;
            }
        }

        TestData data1 = new TestData(new GregorianCalendar(2020, Calendar.JANUARY, 11).getTime());
        TestData data2 = new TestData(new GregorianCalendar(2021, Calendar.FEBRUARY, 12).getTime());
        TestData data3 = new TestData(new GregorianCalendar(2022, Calendar.MARCH, 13).getTime());
        TestData data4 = new TestData(new GregorianCalendar(2023, Calendar.APRIL, 14).getTime());
        TestData data5 = new TestData(new GregorianCalendar(2024, Calendar.MAY, 15).getTime());
        TestData data6 = new TestData(new GregorianCalendar(2025, Calendar.JUNE, 16).getTime());

        ObjectRepository<TestData> repository = db.getRepository(TestData.class);
        repository.insert(data1, data2, data3, data4, data5, data6);

        Cursor<TestData> cursor = repository.find(where("age").between(
            new GregorianCalendar(2020, Calendar.JANUARY, 11).getTime(),
            new GregorianCalendar(2025, Calendar.JUNE, 16).getTime()));
        assertEquals(cursor.size(), 6);

        cursor = repository.find(where("age").between(
            new GregorianCalendar(2020, Calendar.JANUARY, 11).getTime(),
            new GregorianCalendar(2025, Calendar.JUNE, 16).getTime(), false));
        assertEquals(cursor.size(), 4);

        cursor = repository.find(where("age").between(
            new GregorianCalendar(2020, Calendar.JANUARY, 11).getTime(),
            new GregorianCalendar(2025, Calendar.JUNE, 16).getTime(), false, true));
        assertEquals(cursor.size(), 5);
    }
}
