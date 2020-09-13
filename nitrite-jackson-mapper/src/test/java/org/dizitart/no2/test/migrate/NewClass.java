package org.dizitart.no2.test.migrate;

import lombok.Data;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.annotations.Entity;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;

/**
 * @author Anindya Chatterjee
 */
@Data
@Entity(value = "new", indices = {
    @Index(value = "familyName", type = IndexType.NonUnique),
    @Index(value = "fullName", type = IndexType.NonUnique),
    @Index(value = "literature.ratings", type = IndexType.NonUnique),
})
public class NewClass {
    @Id
    private Long empId;
    private String firstName;
    private String familyName;
    private String fullName;
    private Literature literature;


    @Data
    public static class Literature {
        private String text;
        private Integer ratings;
    }
}
