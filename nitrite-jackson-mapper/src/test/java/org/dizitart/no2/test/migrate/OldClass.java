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
@Entity(value = "old", indices = {
    @Index(value = "firstName", type = IndexType.NonUnique),
    @Index(value = "lastName", type = IndexType.NonUnique),
    @Index(value = "literature.text", type = IndexType.Fulltext),
    @Index(value = "literature.ratings", type = IndexType.NonUnique),
})
public class OldClass {
    @Id
    private String uuid;
    private String empId;
    private String firstName;
    private String lastName;
    private Literature literature;


    @Data
    public static class Literature {
        private String text;
        private Float ratings;
    }
}
