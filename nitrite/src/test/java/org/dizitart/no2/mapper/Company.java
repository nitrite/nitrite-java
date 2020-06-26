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

package org.dizitart.no2.mapper;

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

    public static TypeConverter<Company> getConverter() {
        return new TypeConverter<>(
            Company.class,
            (source, mapper) -> Document.createDocument("id", source.id)
                .put("name", source.name)
                .put("companyId", mapper.convert(source.companyId, Document.class)),
            (source, mapper) -> {
                Company company = new Company();
                company.name = source.get("name", String.class);
                company.id = source.get("id", Long.class);
                company.companyId = source.get("companyId", CompanyId.class);
                return company;
            }
        );
    }

    @Data
    public static class CompanyId implements Comparable<CompanyId>, Serializable {
        private Long idValue;

        public CompanyId(long value) {
            this.idValue = value;
        }

        public static TypeConverter<CompanyId> getConverter() {
            return new TypeConverter<>(
                CompanyId.class,
                (source, mapper) -> Document.createDocument("idValue", source.idValue),
                (source, mapper) -> new CompanyId(source.get("idValue", Long.class))
            );
        }

        @Override
        public int compareTo(CompanyId other) {
            return idValue.compareTo(other.idValue);
        }
    }
}
