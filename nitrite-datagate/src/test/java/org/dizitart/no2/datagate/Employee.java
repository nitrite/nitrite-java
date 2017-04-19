package org.dizitart.no2.datagate;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Anindya Chatterjee.
 */
@ToString
@EqualsAndHashCode
@Indices({
        @Index(value = "joinDate", type = IndexType.NonUnique),
        @Index(value = "address", type = IndexType.Fulltext)
})
public class Employee implements Serializable {
    @Id
    @Getter
    @Setter
    private Long empId;

    @Getter
    @Setter
    private Date joinDate;

    @Getter
    @Setter
    private String address;

    @Getter
    @Setter
    private transient List<Employee> subordinates;

    @Getter
    @Setter
    private byte[] blob;

    public Employee() {}
}
