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

import java.util.UUID;

@EqualsAndHashCode
@ToString
public class ClassA {
    @Getter
    @Setter
    private ClassB b;
    @Getter
    @Setter
    private UUID uid;
    @Getter
    @Setter
    private String string;
    @Getter
    @Setter
    private byte[] blob;

    public static ClassA create(int seed) {
        ClassB classB = ClassB.create(seed);
        ClassA classA = new ClassA();
        classA.b = classB;
        classA.uid = new UUID(seed, seed + 50);
        classA.string = Integer.toHexString(seed);
        classA.blob = new byte[]{(byte) seed};
        return classA;
    }

    public static class ClassAConverter implements EntityConverter<ClassA> {

        @Override
        public Class<ClassA> getEntityType() {
            return ClassA.class;
        }

        @Override
        public Document toDocument(ClassA entity, NitriteMapper nitriteMapper) {
            return Document.createDocument()
                .put("b", nitriteMapper.convert(entity.b, Document.class))
                .put("uid", entity.uid)
                .put("string", entity.string)
                .put("blob", entity.blob);
        }

        @Override
        public ClassA fromDocument(Document document, NitriteMapper nitriteMapper) {
            ClassA entity = new ClassA();
            if (document.get("b") != null) {
                Document doc = document.get("b", Document.class);
                entity.b = nitriteMapper.convert(doc, ClassB.class);
            }
            entity.uid = document.get("uid", UUID.class);
            entity.string = document.get("string", String.class);
            entity.blob = document.get("blob", byte[].class);
            return entity;
        }
    }
}
