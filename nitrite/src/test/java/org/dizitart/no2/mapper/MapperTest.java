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

package org.dizitart.no2.mapper;

import org.dizitart.no2.Document;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee
 */
public class MapperTest {
    private GenericMapper jacksonMapper;

    @Before
    public void setUp() {
        jacksonMapper = new JacksonMapper();
    }

    @Test
    public void testNormal() {
        Employee boss = new Employee();
        boss.setEmpId("1");
        boss.setName("Boss");
        boss.setJoiningDate(new Date());

        Employee emp1 = new Employee();
        emp1.setEmpId("abcd");
        emp1.setName("Emp1");
        emp1.setJoiningDate(new Date());
        emp1.setBoss(boss);

        long start = System.currentTimeMillis();
        Document document = jacksonMapper.asDocument(emp1);
        long diff = System.currentTimeMillis() - start;
        System.out.println(diff);

        start = System.currentTimeMillis();
        Employee employee = jacksonMapper.asObject(document, Employee.class);
        diff = System.currentTimeMillis() - start;
        System.out.println(diff);
        assertEquals(emp1, employee);
    }

    @Test
    public void testMappable() {
        MappableEmployee boss = new MappableEmployee();
        boss.setEmpId("1");
        boss.setName("Boss");
        boss.setJoiningDate(new Date());

        MappableEmployee emp1 = new MappableEmployee();
        emp1.setEmpId("abcd");
        emp1.setName("Emp1");
        emp1.setJoiningDate(new Date());
        emp1.setBoss(boss);

        long start = System.currentTimeMillis();
        Document document = jacksonMapper.asDocument(emp1);
        long diff = System.currentTimeMillis() - start;
        System.out.println(diff);

        start = System.currentTimeMillis();
        MappableEmployee employee = jacksonMapper.asObject(document, MappableEmployee.class);
        diff = System.currentTimeMillis() - start;
        System.out.println(diff);
        assertEquals(emp1, employee);
    }

    @Test
    public void testMixed() {
        final MappableEmployee boss = new MappableEmployee();
        boss.setEmpId("1");
        boss.setName("Boss");
        boss.setJoiningDate(new Date());

        final MappableEmployee emp1 = new MappableEmployee();
        emp1.setEmpId("abcd");
        emp1.setName("Emp1");
        emp1.setJoiningDate(new Date());
        emp1.setBoss(boss);

        Department department = new Department();
        department.setName("Dept");
        department.setEmployeeList(new ArrayList<MappableEmployee>() {{ add(boss); add(emp1); }});

        long start = System.currentTimeMillis();
        Document document = jacksonMapper.asDocument(department);
        long diff = System.currentTimeMillis() - start;
        System.out.println(diff);

        start = System.currentTimeMillis();
        Department dept = jacksonMapper.asObject(document, Department.class);
        diff = System.currentTimeMillis() - start;
        System.out.println(diff);
        assertEquals(department, dept);
    }

    @Test
    public void testNested() {
        final MappableEmployee boss = new MappableEmployee();
        boss.setEmpId("1");
        boss.setName("Boss");
        boss.setJoiningDate(new Date());

        final MappableEmployee emp1 = new MappableEmployee();
        emp1.setEmpId("abcd");
        emp1.setName("Emp1");
        emp1.setJoiningDate(new Date());
        emp1.setBoss(boss);

        MappableDepartment department = new MappableDepartment();
        department.setName("Dept");
        department.setEmployeeList(new ArrayList<MappableEmployee>() {{ add(boss); add(emp1); }});

        long start = System.currentTimeMillis();
        Document document = jacksonMapper.asDocument(department);
        long diff = System.currentTimeMillis() - start;
        System.out.println(diff);

        start = System.currentTimeMillis();
        MappableDepartment dept = jacksonMapper.asObject(document, MappableDepartment.class);
        diff = System.currentTimeMillis() - start;
        System.out.println(diff);
        assertEquals(department, dept);
    }

    @Test
    public void testParseJson() {
        MapperFacade facade = new JacksonFacade();
        Document document = Document.createDocument("key1", 1)
                .put("key2", "xyz")
                .put("key3", new Date().getTime());
        String json = facade.toJson(document);
        System.out.println(json);

        Document document2 = facade.parse(json);
        assertEquals(document, document2);
    }
}
