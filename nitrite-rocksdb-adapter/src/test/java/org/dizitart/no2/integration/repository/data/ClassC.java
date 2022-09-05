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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;

@EqualsAndHashCode
@ToString
public class ClassC {
    @Getter
    @Setter
    private long id;
    @Getter
    @Setter
    private double digit;
    @Getter
    @Setter
    private ClassA parent;

    public static ClassC create(int seed) {
        ClassC classC = new ClassC();
        classC.id = seed * 5000L;
        classC.digit = seed * 69.65;
        classC.parent = ClassA.create(seed);
        return classC;
    }

    public static class ClassCConverter implements EntityConverter<ClassC> {

        @Override
        public Class<ClassC> getEntityType() {
            return ClassC.class;
        }

        @Override
        public Document toDocument(ClassC entity, NitriteMapper nitriteMapper) {
            return Document.createDocument()
                .put("id", entity.id)
                .put("digit", entity.digit)
                .put("parent", nitriteMapper.convert(entity.parent, Document.class));
        }

        @Override
        public ClassC fromDocument(Document document, NitriteMapper nitriteMapper) {
            ClassC entity = new ClassC();
            if (document.get("id") != null) {
                entity.id = document.get("id", Long.class);
            }

            if (document.get("digit") != null) {
                entity.digit = document.get("digit", Double.class);
            }

            if (document.get("parent") != null) {
                Document doc = document.get("parent", Document.class);
                entity.parent = nitriteMapper.convert(doc, ClassA.class);
            }
            return entity;
        }
    }
}
