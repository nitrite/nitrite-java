/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.common.mapper;

import lombok.Data;
import org.dizitart.no2.collection.Document;

import java.io.Serializable;

/**
 * @author Anindya Chatterjee.
 */
@Data
public class Company {
    private String name;
    private Long id;
    private CompanyId companyId;

    @Data
    public static class CompanyId implements Comparable<CompanyId>, Serializable {
        private Long idValue;

        public CompanyId(long value) {
            this.idValue = value;
        }

        @Override
        public int compareTo(CompanyId other) {
            return idValue.compareTo(other.idValue);
        }

        public static class CompanyIdConverter implements EntityConverter<CompanyId> {

            @Override
            public Class<CompanyId> getEntityType() {
                return CompanyId.class;
            }

            @Override
            public Document toDocument(CompanyId entity, NitriteMapper nitriteMapper) {
                return Document.createDocument().put("idValue", entity.idValue);
            }

            @Override
            public CompanyId fromDocument(Document document, NitriteMapper nitriteMapper) {
                CompanyId entity = new CompanyId(0L);
                entity.idValue = document.get("idValue", Long.class);
                return entity;
            }
        }
    }

    public static class CompanyConverter implements EntityConverter<Company> {
        @Override
        public Class<Company> getEntityType() {
            return Company.class;
        }

        @Override
        public Document toDocument(Company entity, NitriteMapper nitriteMapper) {
            return Document.createDocument("id", entity.id)
                .put("name", entity.name)
                .put("companyId", nitriteMapper.tryConvert(entity.companyId, Document.class));
        }

        @Override
        public Company fromDocument(Document document, NitriteMapper nitriteMapper) {
            Company entity = new Company();
            entity.name = document.get("name", String.class);
            entity.id = document.get("id", Long.class);
            entity.companyId = (CompanyId) nitriteMapper.tryConvert(document.get("companyId", Document.class), CompanyId.class);
            return entity;
        }
    }
}
