/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.collection.objects.data;

import lombok.Data;
import org.dizitart.no2.collection.IndexType;
import org.dizitart.no2.index.annotations.Id;
import org.dizitart.no2.index.annotations.Index;
import org.dizitart.no2.index.annotations.Indices;

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
}
