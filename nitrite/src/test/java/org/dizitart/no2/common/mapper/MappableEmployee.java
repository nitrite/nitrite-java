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
import lombok.ToString;
import org.dizitart.no2.collection.Document;

import java.util.Date;

/**
 * @author Anindya Chatterjee
 */
@Data
@ToString
public class MappableEmployee {
    private String empId;
    private String name;
    private Date joiningDate;
    private MappableEmployee boss;

    public static class MappableEmployeeConverter implements EntityConverter<MappableEmployee> {

        @Override
        public Class<MappableEmployee> getEntityType() {
            return MappableEmployee.class;
        }

        @Override
        public Document toDocument(MappableEmployee entity, NitriteMapper nitriteMapper) {
            Document document = Document.createDocument();
            document.put("empId", entity.getEmpId());
            document.put("name", entity.getName());
            document.put("joiningDate", entity.getJoiningDate());

            if (entity.getBoss() != null) {
                Document bossDoc = (Document) nitriteMapper.tryConvert(entity.getBoss(), Document.class);
                document.put("boss", bossDoc);
            }
            return document;
        }

        @Override
        public MappableEmployee fromDocument(Document document, NitriteMapper nitriteMapper) {
            MappableEmployee entity = new MappableEmployee();

            if (document != null) {
                entity.setEmpId((String) document.get("empId"));
                entity.setName((String) document.get("name"));
                entity.setJoiningDate((Date) document.get("joiningDate"));

                Document bossDoc = (Document) document.get("boss");
                if (bossDoc != null) {
                    MappableEmployee bossEmp = (MappableEmployee) nitriteMapper.tryConvert(bossDoc, MappableEmployee.class);
                    entity.setBoss(bossEmp);
                }
            }

            return entity;
        }
    }
}
