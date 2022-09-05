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
import org.dizitart.no2.repository.annotations.Index;

/**
 * @author Anindya Chatterjee
 */
@Data
@Index(value = "firstName")
@Index(value = "age", type = IndexType.NON_UNIQUE)
@Index(value = "lastName", type = IndexType.FULL_TEXT)
public class RepeatableIndexTest {
    private String firstName;
    private Integer age;
    private String lastName;

    public static class Converter implements EntityConverter<RepeatableIndexTest> {

        @Override
        public Class<RepeatableIndexTest> getEntityType() {
            return RepeatableIndexTest.class;
        }

        @Override
        public Document toDocument(RepeatableIndexTest entity, NitriteMapper nitriteMapper) {
            return Document.createDocument("firstName", entity.firstName)
                .put("age", entity.age)
                .put("lastName", entity.lastName);
        }

        @Override
        public RepeatableIndexTest fromDocument(Document document, NitriteMapper nitriteMapper) {
            RepeatableIndexTest entity = new RepeatableIndexTest();
            entity.firstName = document.get("firstName", String.class);
            entity.age = document.get("age", Integer.class);
            entity.lastName = document.get("lastName", String.class);
            return entity;
        }
    }
}
