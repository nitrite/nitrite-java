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
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;

/**
 * @author Anindya Chatterjee.
 */
@EqualsAndHashCode
public class WithOutGetterSetter {
    private String name;
    private Long number;

    public WithOutGetterSetter() {
        name = "test";
        number = 2L;
    }

    public static class Converter implements EntityConverter<WithOutGetterSetter> {

        @Override
        public Class<WithOutGetterSetter> getEntityType() {
            return WithOutGetterSetter.class;
        }

        @Override
        public Document toDocument(WithOutGetterSetter entity, NitriteMapper nitriteMapper) {
            return Document.createDocument("name", entity.name)
                .put("number", entity.number);
        }

        @Override
        public WithOutGetterSetter fromDocument(Document document, NitriteMapper nitriteMapper) {
            WithOutGetterSetter entity = new WithOutGetterSetter();
            entity.name = document.get("name", String.class);
            entity.number = document.get("number", Long.class);
            return entity;
        }
    }
}
