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

package org.dizitart.no2.common.mapper;

import org.dizitart.no2.integration.Retry;
import org.dizitart.no2.collection.Document;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee
 */
public class MapperTest {
    private EntityConverterMapper entityConverterMapper;

    @Rule
    public Retry retry = new Retry(3);

    @Test
    public void testWithConverter() {
        entityConverterMapper = new EntityConverterMapper();
        entityConverterMapper.registerEntityConverter(new Employee.Converter());

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
        Document document = (Document) entityConverterMapper.tryConvert(emp1, Document.class);
        long diff = System.currentTimeMillis() - start;
        System.out.println(diff);

        start = System.currentTimeMillis();
        Employee employee = (Employee) entityConverterMapper.tryConvert(document, Employee.class);
        diff = System.currentTimeMillis() - start;
        System.out.println(diff);
        assertEquals(emp1, employee);
    }

    @Test
    public void testWithMappable() {
        entityConverterMapper = new EntityConverterMapper();
        entityConverterMapper.registerEntityConverter(new Department.DepartmentConverter());
        entityConverterMapper.registerEntityConverter(new MappableEmployee.MappableEmployeeConverter());
        entityConverterMapper.registerEntityConverter(new MappableDepartment.Converter());

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
        Document document = (Document) entityConverterMapper.tryConvert(emp1, Document.class);
        long diff = System.currentTimeMillis() - start;
        System.out.println(diff);

        start = System.currentTimeMillis();
        MappableEmployee employee = (MappableEmployee) entityConverterMapper.tryConvert(document, MappableEmployee.class);
        diff = System.currentTimeMillis() - start;
        System.out.println(diff);
        assertEquals(emp1, employee);
    }

    @Test
    public void testWithConverterAndMappableMix() {
        entityConverterMapper = new EntityConverterMapper();
        entityConverterMapper.registerEntityConverter(new Department.DepartmentConverter());
        entityConverterMapper.registerEntityConverter(new MappableEmployee.MappableEmployeeConverter());
        entityConverterMapper.registerEntityConverter(new MappableDepartment.Converter());

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
        department.setEmployeeList(new ArrayList<MappableEmployee>() {{
            add(boss);
            add(emp1);
        }});

        long start = System.currentTimeMillis();
        Document document = (Document) entityConverterMapper.tryConvert(department, Document.class);
        long diff = System.currentTimeMillis() - start;
        System.out.println(diff);

        start = System.currentTimeMillis();
        Department dept = (Department) entityConverterMapper.tryConvert(document, Department.class);
        diff = System.currentTimeMillis() - start;
        System.out.println(diff);
        assertEquals(department, dept);
    }

    @Test
    public void testNested() {
        entityConverterMapper = new EntityConverterMapper();
        entityConverterMapper.registerEntityConverter(new Department.DepartmentConverter());
        entityConverterMapper.registerEntityConverter(new MappableEmployee.MappableEmployeeConverter());
        entityConverterMapper.registerEntityConverter(new MappableDepartment.Converter());

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
        department.setEmployeeList(new ArrayList<MappableEmployee>() {{
            add(boss);
            add(emp1);
        }});

        long start = System.currentTimeMillis();
        Document document = (Document) entityConverterMapper.tryConvert(department, Document.class);
        long diff = System.currentTimeMillis() - start;
        System.out.println(diff);

        start = System.currentTimeMillis();
        MappableDepartment dept = (MappableDepartment) entityConverterMapper.tryConvert(document, MappableDepartment.class);
        diff = System.currentTimeMillis() - start;
        System.out.println(diff);
        assertEquals(department, dept);
    }

    @Test
    public void testWithValueType() {
        entityConverterMapper = new EntityConverterMapper();
        entityConverterMapper.registerEntityConverter(new Company.CompanyConverter());
        entityConverterMapper.registerEntityConverter(new Company.CompanyId.CompanyIdConverter());

        Company company = new Company();
        company.setName("test");
        company.setId(1L);
        company.setCompanyId(new Company.CompanyId(1L));

        Document document = (Document) entityConverterMapper.tryConvert(company, Document.class);
        Object companyId = document.get("companyId");
        assertTrue(companyId instanceof Document);
    }
}
