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

package org.dizitart.no2.integration.repository.data;

import lombok.Data;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.annotations.Entity;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;

import java.util.Date;
import java.util.UUID;

/**
 * @author Anindya Chatterjee
 */
@Data
@Entity(value = "MyPerson", indices = {
    @Index(fields = "name", type = IndexType.FULL_TEXT),
    @Index(fields = "status", type = IndexType.NON_UNIQUE)
})
public class PersonEntity {
    @Id
    private String uuid;
    private String name;
    private String status;
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

    public static class Converter implements EntityConverter<PersonEntity> {

        @Override
        public Class<PersonEntity> getEntityType() {
            return PersonEntity.class;
        }

        @Override
        public Document toDocument(PersonEntity entity, NitriteMapper nitriteMapper) {
            return Document.createDocument("uuid", entity.uuid)
                .put("name", entity.name)
                .put("status", entity.status)
                .put("friend", entity.friend != null ? nitriteMapper.tryConvert(entity.friend, Document.class) : null)
                .put("dateCreated", entity.dateCreated);
        }

        @Override
        public PersonEntity fromDocument(Document document, NitriteMapper nitriteMapper) {
            if (document != null) {
                PersonEntity entity = new PersonEntity();
                entity.uuid = document.get("uuid", String.class);
                entity.name = document.get("name", String.class);
                entity.status = document.get("status", String.class);
                entity.dateCreated = document.get("dateCreated", Date.class);
                entity.friend = (PersonEntity) nitriteMapper.tryConvert(document.get("friend", Document.class), PersonEntity.class);
                return entity;
            }
            return null;
        }
    }
}
