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
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;

import java.util.Date;

/**
 * @author Anindya Chatterjee.
 */
@EqualsAndHashCode
public class SubEmployee {
    @Getter
    @Setter
    private Long empId;

    @Getter
    @Setter
    private Date joinDate;

    @Getter
    @Setter
    private String address;

    public static class Converter implements EntityConverter<SubEmployee> {

        @Override
        public Class<SubEmployee> getEntityType() {
            return SubEmployee.class;
        }

        @Override
        public Document toDocument(SubEmployee entity, NitriteMapper nitriteMapper) {
            return Document.createDocument()
                .put("empId", entity.empId)
                .put("joinDate", entity.joinDate)
                .put("address", entity.address);
        }

        @Override
        public SubEmployee fromDocument(Document document, NitriteMapper nitriteMapper) {
            SubEmployee entity = new SubEmployee();
            entity.empId = document.get("empId", Long.class);
            entity.joinDate = document.get("joinDate", Date.class);
            entity.address = document.get("address", String.class);
            return entity;
        }
    }
}
