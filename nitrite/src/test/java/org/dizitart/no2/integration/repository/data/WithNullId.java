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

import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.repository.annotations.Id;

/**
 * @author Anindya Chatterjee.
 */
@Getter
@Setter
public class WithNullId {
    @Id
    private String name;
    private Long number;

    public static class Converter implements EntityConverter<WithNullId> {

        @Override
        public Class<WithNullId> getEntityType() {
            return WithNullId.class;
        }

        @Override
        public Document toDocument(WithNullId entity, NitriteMapper nitriteMapper) {
            return Document.createDocument()
                .put("name", entity.name)
                .put("number", entity.number);
        }

        @Override
        public WithNullId fromDocument(Document document, NitriteMapper nitriteMapper) {
            WithNullId entity = new WithNullId();
            entity.name = document.get("name", String.class);
            entity.number = document.get("number", Long.class);
            return entity;
        }
    }
}
