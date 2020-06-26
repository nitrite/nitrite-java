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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;
import org.dizitart.no2.repository.annotations.Indices;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Anindya Chatterjee.
 */
@ToString
@EqualsAndHashCode
@Indices({
    @Index(value = "companyName")
})
public class Company implements Serializable, Mappable {
    @Id
    @Getter
    @Setter
    private Long companyId;

    @Getter
    @Setter
    private String companyName;

    @Getter
    @Setter
    private Date dateCreated;

    @Getter
    @Setter
    private List<String> departments;

    @Getter
    @Setter
    private Map<String, List<Employee>> employeeRecord;

    @Override
    public Document write(NitriteMapper mapper) {
        return Document.createDocument("companyId", companyId)
            .put("companyName", companyName)
            .put("dateCreated", dateCreated)
            .put("departments", departments)
            .put("employeeRecord", employeeRecord);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(NitriteMapper mapper, Document document) {
        companyId = document.get("companyId", Long.class);
        companyName = document.get("companyName", String.class);
        dateCreated = document.get("dateCreated", Date.class);
        departments = document.get("departments", List.class);
        employeeRecord = document.get("employeeRecord", Map.class);
    }
}
