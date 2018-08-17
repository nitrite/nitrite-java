/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.mapper;

import lombok.Data;
import lombok.ToString;
import org.dizitart.no2.Document;

import java.util.Date;

/**
 * @author Anindya Chatterjee
 */
@Data
@ToString
public class MappableEmployee implements Mappable {
    private String empId;
    private String name;
    private Date joiningDate;
    private MappableEmployee boss;

    @Override
    public Document write(NitriteMapper mapper) {
        Document document = new Document();
        document.put("empId", getEmpId());
        document.put("name", getName());
        document.put("joiningDate", getJoiningDate());

        if (getBoss() != null) {
            Document bossDoc = getBoss().write(mapper);
            document.put("boss", bossDoc);
        }
        return document;
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        if (document != null) {
            setEmpId((String) document.get("empId"));
            setName((String) document.get("name"));
            setJoiningDate((Date) document.get("joiningDate"));

            Document bossDoc = (Document) document.get("boss");
            if (bossDoc != null) {
                MappableEmployee bossEmp = new MappableEmployee();
                bossEmp.read(mapper, bossDoc);
                setBoss(bossEmp);
            }
        }
    }
}
