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

package org.dizitart.no2.integration.repository;

import lombok.Data;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.repository.annotations.Id;

/**
 * @author Anindya Chatterjee.
 */
@Data
class InternalClass {
    @Id
    private Long id;
    private String name;

    public static class Converter implements EntityConverter<InternalClass> {
        @Override
        public Class<InternalClass> getEntityType() {
            return InternalClass.class;
        }

        @Override
        public Document toDocument(InternalClass entity, NitriteMapper nitriteMapper) {
            return Document.createDocument("id", entity.id)
                .put("name", entity.name);
        }

        @Override
        public InternalClass fromDocument(Document document, NitriteMapper nitriteMapper) {
            InternalClass entity = new InternalClass();
            entity.id = document.get("id", Long.class);
            entity.name = document.get("name", String.class);
            return entity;
        }
    }
}
