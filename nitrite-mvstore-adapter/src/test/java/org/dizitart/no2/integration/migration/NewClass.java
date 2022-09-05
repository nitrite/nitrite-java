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
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.annotations.Entity;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;

/**
 * @author Anindya Chatterjee
 */
@Data
@Entity(value = "new", indices = {
    @Index(value = "familyName", type = IndexType.NON_UNIQUE),
    @Index(value = "fullName", type = IndexType.NON_UNIQUE),
    @Index(value = "literature.ratings", type = IndexType.NON_UNIQUE),
})
public class NewClass {
    @Id
    private Long empId;
    private String firstName;
    private String familyName;
    private String fullName;
    private Literature literature;

    public static class Converter implements EntityConverter<NewClass> {

        @Override
        public Class<NewClass> getEntityType() {
            return NewClass.class;
        }

        @Override
        public Document toDocument(NewClass entity, NitriteMapper nitriteMapper) {
            return Document.createDocument("empId", entity.empId)
                .put("firstName", entity.firstName)
                .put("familyName", entity.familyName)
                .put("fullName", entity.fullName)
                .put("literature", nitriteMapper.convert(entity.literature, Document.class));
        }

        @Override
        public NewClass fromDocument(Document document, NitriteMapper nitriteMapper) {
            NewClass entity = new NewClass();
            entity.empId = document.get("empId", Long.class);
            entity.firstName = document.get("firstName", String.class);
            entity.familyName = document.get("familyName", String.class);
            entity.fullName = document.get("fullName", String.class);

            Document doc = document.get("literature", Document.class);
            entity.literature = nitriteMapper.convert(doc, Literature.class);
            return entity;
        }
    }

    @Data
    public static class Literature {
        private String text;
        private Integer ratings;

        public static class Converter implements EntityConverter<Literature> {

            @Override
            public Class<Literature> getEntityType() {
                return Literature.class;
            }

            @Override
            public Document toDocument(Literature entity, NitriteMapper nitriteMapper) {
                return Document.createDocument("text", entity.text)
                    .put("ratings", entity.ratings);
            }

            @Override
            public Literature fromDocument(Document document, NitriteMapper nitriteMapper) {
                Literature entity = new Literature();
                entity.text = document.get("text", String.class);
                entity.ratings = document.get("ratings", Integer.class);
                return entity;
            }
        }
    }
}
