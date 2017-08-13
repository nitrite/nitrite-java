package org.dizitart.no2.objects.data;

import lombok.Data;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

import java.util.Date;
import java.util.UUID;

/**
 * @author Anindya Chatterjee
 */
@Data
@Indices({
        @Index(value = "name", type = IndexType.Fulltext)
})
public class PersonEntity {
    @Id
    private String uuid;
    private String name;
    private PersonEntity friend;
    private Date dateCreated;

    public PersonEntity() {
        this.uuid = UUID.randomUUID().toString();
        this.dateCreated = new Date();
    }

    public PersonEntity(String name) {
        this.uuid = UUID.randomUUID().toString();
        this.name = name;
        this.dateCreated = new Date();
    }
}
