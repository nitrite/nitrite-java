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

import java.util.Date;

/**
 * @author Anindya Chatterjee
 */
@Data
@ToString
public class Employee implements Mappable {
    private String empId;
    private String name;
    private Date joiningDate;
    private Employee boss;

    @Override
    public Document write(NitriteMapper mapper) {
        Document document = Document.createDocument("empId", getEmpId())
            .put("name", getName())
            .put("joiningDate", getJoiningDate());

        if (getBoss() != null) {
            document.put("boss", mapper.convert(getBoss(), Document.class));
        }
        return document;
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        setEmpId(document.get("empId", String.class));
        setName(document.get("name", String.class));
        setJoiningDate(document.get("joiningDate", Date.class));

        Document bossDoc = document.get("boss", Document.class);
        if (bossDoc != null) {
            Employee boss = mapper.convert(bossDoc, Employee.class);
            setBoss(boss);
        }
    }
}
