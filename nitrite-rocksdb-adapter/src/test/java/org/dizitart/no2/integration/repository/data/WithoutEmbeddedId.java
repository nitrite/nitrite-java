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
import org.dizitart.no2.repository.annotations.Id;

/**
 * @author Anindya Chatterjee
 */
@Data
public class WithoutEmbeddedId {
    @Id
    private NestedId nestedId;
    private String data;

    @Data
    public static class NestedId {
        private Long id;

        public static class Converter implements EntityConverter<NestedId> {

            @Override
            public Class<NestedId> getEntityType() {
                return NestedId.class;
            }

            @Override
            public Document toDocument(NestedId entity, NitriteMapper nitriteMapper) {
                return Document.createDocument()
                    .put("id", entity.id);
            }

            @Override
            public NestedId fromDocument(Document document, NitriteMapper nitriteMapper) {
                NestedId entity = new NestedId();
                entity.id = document.get("id", Long.class);
                return entity;
            }
        }
    }

    public static class Converter implements EntityConverter<WithoutEmbeddedId> {

        @Override
        public Class<WithoutEmbeddedId> getEntityType() {
            return WithoutEmbeddedId.class;
        }

        @Override
        public Document toDocument(WithoutEmbeddedId entity, NitriteMapper nitriteMapper) {
            return Document.createDocument()
                .put("nestedId", nitriteMapper.convert(entity.nestedId, Document.class))
                .put("data", entity.data);
        }

        @Override
        public WithoutEmbeddedId fromDocument(Document document, NitriteMapper nitriteMapper) {
            WithoutEmbeddedId entity = new WithoutEmbeddedId();
            Document nestedId = document.get("nestedId", Document.class);

            entity.nestedId = nitriteMapper.convert(nestedId, NestedId.class);
            entity.data = document.get("data", String.class);

            return entity;
        }
    }
}
