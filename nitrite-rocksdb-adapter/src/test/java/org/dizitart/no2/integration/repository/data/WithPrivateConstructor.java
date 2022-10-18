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
public class WithPrivateConstructor {
    private String name;
    private Long number;

    private WithPrivateConstructor() {
        name = "test";
        number = 2L;
    }

    public static WithPrivateConstructor create(final String name, final Long number) {
        WithPrivateConstructor obj = new WithPrivateConstructor();
        obj.number = number;
        obj.name = name;
        return obj;
    }

    public static class Converter implements EntityConverter<WithPrivateConstructor> {

        @Override
        public Class<WithPrivateConstructor> getEntityType() {
            return WithPrivateConstructor.class;
        }

        @Override
        public Document toDocument(WithPrivateConstructor entity, NitriteMapper nitriteMapper) {
            return Document.createDocument("name", entity.name)
                .put("number", entity.number);
        }

        @Override
        public WithPrivateConstructor fromDocument(Document document, NitriteMapper nitriteMapper) {
            String name = document.get("name", String.class);
            Long number = document.get("number", Long.class);
            return WithPrivateConstructor.create(name, number);
        }
    }
}
