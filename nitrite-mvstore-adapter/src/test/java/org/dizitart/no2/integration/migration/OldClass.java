/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.integration.migration;

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
@Entity(value = "old", indices = {
    @Index(value = "firstName", type = IndexType.NON_UNIQUE),
    @Index(value = "lastName", type = IndexType.NON_UNIQUE),
    @Index(value = "literature.text", type = IndexType.FULL_TEXT),
    @Index(value = "literature.ratings", type = IndexType.NON_UNIQUE),
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
