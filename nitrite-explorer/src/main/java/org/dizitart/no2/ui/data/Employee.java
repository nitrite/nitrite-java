package org.dizitart.no2.ui.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Anindya Chatterjee.
 */
@EqualsAndHashCode
@Indices({
        @Index(value = "joinDate", type = IndexType.NonUnique),
        @Index(value = "address", type = IndexType.Fulltext),
        @Index(value = "employeeNote.text", type = IndexType.Fulltext)
})
public class Employee implements Serializable {
    @Id
    @Getter
    @Setter
    private long empId;

    @Getter
    @Setter
    private Date joinDate;

    @Getter
    @Setter
    private String address;

    @Getter
    @Setter
    private transient Company company;

    @Getter
    @Setter
    private byte[] blob;

    @Getter
    @Setter
    private Note employeeNote;
}
