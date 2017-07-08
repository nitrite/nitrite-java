package org.dizitart.no2.mapper;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * @author Anindya Chatterjee
 */
@Data
@ToString
public class Employee {
    private String empId;
    private String name;
    private Date joiningDate;
    private Employee boss;
}
