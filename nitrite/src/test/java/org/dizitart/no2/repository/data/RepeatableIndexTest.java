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
import org.dizitart.no2.repository.annotations.Index;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;

/**
 * @author Anindya Chatterjee
 */
@Data
@Index(value = "firstName")
@Index(value = "age", type = IndexType.NonUnique)
@Index(value = "lastName", type = IndexType.Fulltext)
public class RepeatableIndexTest implements Mappable {
    private String firstName;
    private Integer age;
    private String lastName;

    @Override
    public Document write(NitriteMapper mapper) {
        return Document.createDocument("firstName", firstName)
            .put("age", age)
            .put("lastName", lastName);
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        firstName = document.get("firstName", String.class);
        age = document.get("age", Integer.class);
        lastName = document.get("lastName", String.class);
    }
}
