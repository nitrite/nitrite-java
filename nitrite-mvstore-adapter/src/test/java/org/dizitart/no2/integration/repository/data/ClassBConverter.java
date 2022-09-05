/*
 * Copyright (c) 2017-2022 Nitrite author or authors.
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

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;

public class ClassBConverter implements EntityConverter<ClassB> {

    @Override
    public Class<ClassB> getEntityType() {
        return ClassB.class;
    }

    @Override
    public Document toDocument(ClassB entity, NitriteMapper nitriteMapper) {
        return Document.createDocument()
            .put("number", entity.getNumber())
            .put("text", entity.getText());
    }

    @Override
    public ClassB fromDocument(Document document, NitriteMapper nitriteMapper) {
        ClassB entity = new ClassB();
        if (document.get("number") != null) {
            entity.setNumber(document.get("number", Integer.class));
        }
        entity.setText(document.get("text", String.class));
        return entity;
    }
}
