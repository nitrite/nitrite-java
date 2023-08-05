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

import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;

/**
 * @author Anindya Chatterjee.
 */
@Getter
@Setter
public class StressRecord {
    private String firstName;
    private Boolean processed;
    private String lastName;
    private Boolean failed;
    private String notes;

    public static class Converter implements EntityConverter<StressRecord> {
        @Override
        public Class<StressRecord> getEntityType() {
            return StressRecord.class;
        }

        @Override
        public Document toDocument(StressRecord entity, NitriteMapper nitriteMapper) {
            return Document.createDocument().put("firstName", entity.firstName)
                .put("processed", entity.processed)
                .put("lastName", entity.lastName)
                .put("failed", entity.failed)
                .put("notes", entity.notes);
        }

        @Override
        public StressRecord fromDocument(Document document, NitriteMapper nitriteMapper) {
            StressRecord entity = new StressRecord();
            entity.firstName = document.get("firstName", String.class);
            entity.processed = document.get("processed", Boolean.class);
            entity.lastName = document.get("lastName", String.class);
            entity.failed = document.get("failed", Boolean.class);
            entity.notes = document.get("notes", String.class);
            return entity;
        }
    }
}
