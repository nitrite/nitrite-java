package org.dizitart.no2.ui.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Anindya Chatterjee.
 */
@EqualsAndHashCode
@Index(value = "companyName")
public class Company implements Serializable {
    @Id
    @Getter
    @Setter
    private long companyId;

    @Getter
    @Setter
    private String companyName;

    @Getter
    @Setter
    private Date dateCreated;

    @Getter
    @Setter
    private List<String> departments;

    @Getter
    @Setter
    private Map<String, List<Employee>> employeeRecord;
}
