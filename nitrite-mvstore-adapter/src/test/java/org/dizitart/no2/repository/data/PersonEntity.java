/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.repository.data;

import lombok.Data;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;
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
    @Index(value = "name", type = IndexType.Fulltext),
    @Index(value = "status", type = IndexType.NonUnique)
})
public class PersonEntity implements Mappable {
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

    @Override
    public Document write(NitriteMapper mapper) {
        return Document.createDocument("uuid", uuid)
            .put("name", name)
            .put("status", status)
            .put("friend", friend != null ? friend.write(mapper) : null)
            .put("dateCreated", dateCreated);
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        if (document != null) {
            uuid = document.get("uuid", String.class);
            name = document.get("name", String.class);
            status = document.get("status", String.class);
            dateCreated = document.get("dateCreated", Date.class);
            friend = new PersonEntity();
            friend.read(mapper, document.get("friend", Document.class));
        }
    }
}
