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

package org.dizitart.no2.support;

import lombok.Data;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;
import org.dizitart.no2.repository.annotations.Indices;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Anindya Chatterjee.
 */
@Data
@Indices({
    @Index(value = "companyName")
})
public class Company implements Serializable {
    @Id(fieldName = "company_id")
    private Long companyId;

    private String companyName;

    private Date dateCreated;

    private List<String> departments;

    private Map<String, List<Employee>> employeeRecord;

    @Override
    public String toString() {
        return "Company{" +
            "companyId=" + companyId +
            ", companyName='" + companyName + '\'' +
            ", dateCreated=" + dateCreated +
            ", departments=" + departments +
            '}';
    }

    public static class CompanyConverter implements EntityConverter<Company> {

        @Override
        public Class<Company> getEntityType() {
            return Company.class;
        }

        @Override
        public Document toDocument(Company entity, NitriteMapper nitriteMapper) {
            return Document.createDocument("company_id", entity.companyId)
                .put("companyName", entity.companyName)
                .put("dateCreated", entity.dateCreated)
                .put("departments", entity.departments)
                .put("employeeRecord", entity.employeeRecord);
        }

        @Override
        public Company fromDocument(Document document, NitriteMapper nitriteMapper) {
            Company entity = new Company();
            entity.companyId = document.get("company_id", Long.class);
            entity.companyName = document.get("companyName", String.class);
            entity.dateCreated = document.get("dateCreated", Date.class);
            entity.departments = document.get("departments", List.class);
            entity.employeeRecord = document.get("employeeRecord", Map.class);
            return entity;
        }
    }
}
