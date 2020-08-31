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

package org.dizitart.no2.mapdb.mapper;

import lombok.Data;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;

import java.io.Serializable;

/**
 * @author Anindya Chatterjee.
 */
@Data
public class Company implements Mappable {
    private String name;
    private Long id;
    private CompanyId companyId;

    @Override
    public Document write(NitriteMapper mapper) {
        return Document.createDocument("id", id)
            .put("name", name)
            .put("companyId", mapper.convert(companyId, Document.class));
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        name = document.get("name", String.class);
        id = document.get("id", Long.class);
        companyId = document.get("companyId", CompanyId.class);
    }

    @Data
    public static class CompanyId implements Comparable<CompanyId>, Serializable, Mappable {
        private Long idValue;

        public CompanyId(long value) {
            this.idValue = value;
        }

        @Override
        public int compareTo(CompanyId other) {
            return idValue.compareTo(other.idValue);
        }

        @Override
        public Document write(NitriteMapper mapper) {
            return Document.createDocument().put("idValue", idValue);
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            idValue = document.get("idValue", Long.class);
        }
    }
}
