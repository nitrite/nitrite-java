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
    private JacksonMapper jacksonMapper;

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
}
