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

package org.dizitart.no2.rocksdb.repository.data;

import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.Mappable;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.repository.annotations.Id;

/**
 * @author Anindya Chatterjee.
 */
@Getter
@Setter
public class StressRecord implements Mappable {
    @Id
    private String firstName;
    private boolean processed;
    private String lastName;
    private boolean failed;
    private String notes;

    @Override
    public Document write(NitriteMapper mapper) {
        return Document.createDocument().put("firstName", firstName)
            .put("processed", processed)
            .put("lastName", lastName)
            .put("failed", failed)
            .put("notes", notes);
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        firstName = document.get("firstName", String.class);
        processed = document.get("processed", Boolean.class);
        lastName = document.get("lastName", String.class);
        failed = document.get("failed", Boolean.class);
        notes = document.get("notes", String.class);
    }
}
