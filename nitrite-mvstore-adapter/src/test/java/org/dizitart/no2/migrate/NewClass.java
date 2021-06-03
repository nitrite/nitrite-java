package org.dizitart.no2.migrate;

import lombok.Data;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.common.mapper.Mappable;
import org.dizitart.no2.common.mapper.NitriteMapper;
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
public class NewClass implements Mappable {
    @Id
    private Long empId;
    private String firstName;
    private String familyName;
    private String fullName;
    private Literature literature;

    @Override
    public Document write(NitriteMapper mapper) {
        return Document.createDocument("empId", empId)
            .put("firstName", firstName)
            .put("familyName", familyName)
            .put("fullName", fullName)
            .put("literature", literature.write(mapper));
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        empId = document.get("empId", Long.class);
        firstName = document.get("firstName", String.class);
        familyName = document.get("familyName", String.class);
        fullName = document.get("fullName", String.class);

        Document doc = document.get("literature", Document.class);
        literature = new Literature();
        literature.read(mapper, doc);
    }

    @Data
    public static class Literature implements Mappable {
        private String text;
        private Integer ratings;

        @Override
        public Document write(NitriteMapper mapper) {
            return Document.createDocument("text", text)
                .put("ratings", ratings);
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            text = document.get("text", String.class);
            ratings = document.get("ratings", Integer.class);
        }
    }
}
