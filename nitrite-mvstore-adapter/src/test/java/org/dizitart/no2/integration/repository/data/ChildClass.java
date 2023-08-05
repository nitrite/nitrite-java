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
import org.dizitart.no2.repository.annotations.InheritIndices;

import java.util.Date;

/**
 * @author Anindya Chatterjee
 */
@Getter
@Setter
@InheritIndices
public class ChildClass extends ParentClass {
    private String name;

    public static class Converter implements EntityConverter<ChildClass> {

        @Override
        public Class<ChildClass> getEntityType() {
            return ChildClass.class;
        }

        @Override
        public Document toDocument(ChildClass entity, NitriteMapper nitriteMapper) {
            return Document.createDocument()
                .put("name", entity.getName())
                .put("id", entity.getId())
                .put("date", entity.getDate())
                .put("text", entity.getText());
        }

        @Override
        public ChildClass fromDocument(Document document, NitriteMapper nitriteMapper) {
            ChildClass entity = new ChildClass();
            entity.setId(document.get("id", Long.class));
            entity.setDate(document.get("date", Date.class));
            entity.setText(document.get("text", String.class));
            entity.setName(document.get("name", String.class));
            return entity;
        }
    }
}
