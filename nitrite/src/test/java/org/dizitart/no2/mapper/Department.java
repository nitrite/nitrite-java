package org.dizitart.no2.mapper;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author Anindya Chatterjee
 */
@Data
@ToString
public class Department {
    private String name;
    private List<MappableEmployee> employeeList;
}
