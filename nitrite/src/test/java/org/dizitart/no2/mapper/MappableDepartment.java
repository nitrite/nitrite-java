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
import lombok.ToString;
import org.dizitart.no2.collection.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anindya Chatterjee
 */
@Data
@ToString
public class MappableDepartment implements Mappable {
    private String name;
    private List<MappableEmployee> employeeList;

    @Override
    public Document write(NitriteMapper mapper) {
        Document document = Document.createDocument();

        document.put("name", getName());
        List<Document> employees = new ArrayList<>();
        for (MappableEmployee employee : getEmployeeList()) {
            employees.add(employee.write(mapper));
        }
        document.put("employeeList", employees);

        return document;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(NitriteMapper mapper, Document document) {
        if (document != null) {
            setName((String) document.get("name"));
            for (Document doc : (List<Document>) document.get("employeeList")) {
                MappableEmployee me = new MappableEmployee();
                me.read(mapper, doc);
                getEmployeeList().add(me);
            }
        }
    }

    private List<MappableEmployee> getEmployeeList() {
        if (employeeList == null) {
            employeeList = new ArrayList<>();
        }
        return employeeList;
    }
}
