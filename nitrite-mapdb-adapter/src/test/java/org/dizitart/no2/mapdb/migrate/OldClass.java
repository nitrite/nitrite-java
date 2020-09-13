package org.dizitart.no2.mapdb.migrate;

import lombok.Data;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;
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
public class OldClass implements Mappable {
    @Id
    private String uuid;
    private String empId;
    private String firstName;
    private String lastName;
    private Literature literature;

    @Override
    public Document write(NitriteMapper mapper) {
        return Document.createDocument("empId", empId)
            .put("uuid", uuid)
            .put("firstName", firstName)
            .put("lastName", lastName)
            .put("literature", literature.write(mapper));
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        empId = document.get("empId", String.class);
        uuid = document.get("uuid", String.class);
        firstName = document.get("firstName", String.class);
        lastName = document.get("lastName", String.class);

        Document doc = document.get("literature", Document.class);
        literature = new Literature();
        literature.read(mapper, doc);
    }

    @Data
    public static class Literature implements Mappable {
        private String text;
        private Float ratings;

        @Override
        public Document write(NitriteMapper mapper) {
            return Document.createDocument("text", text)
                .put("ratings", ratings);
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            text = document.get("text", String.class);
            ratings = document.get("ratings", Float.class);
        }
    }
}
