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
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.repository.annotations.Id;

/**
 * @author Anindya Chatterjee
 */
@Data
public class WithNitriteId {
    @Id
    public NitriteId idField;
    public String name;

    public static class WithNitriteIdConverter implements EntityConverter<WithNitriteId> {

        @Override
        public Class<WithNitriteId> getEntityType() {
            return WithNitriteId.class;
        }

        @Override
        public Document toDocument(WithNitriteId entity, NitriteMapper nitriteMapper) {
            return Document.createDocument()
                .put("idField", entity.idField)
                .put("name", entity.name);
        }

        @Override
        public WithNitriteId fromDocument(Document document, NitriteMapper nitriteMapper) {
            WithNitriteId entity = new WithNitriteId();
            entity.idField = document.get("idField", NitriteId.class);
            entity.name = document.get("name", String.class);
            return entity;
        }
    }
}
