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

import lombok.Data;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.Mappable;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.repository.annotations.Id;

/**
 * @author Anindya Chatterjee
 */
@Data
public class WithoutEmbeddedId implements Mappable {
    @Id
    private NestedId nestedId;
    private String data;

    @Override
    public Document write(NitriteMapper mapper) {
        return Document.createDocument()
            .put("nestedId", nestedId.write(mapper))
            .put("data", data);
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        Document nestedId = document.get("nestedId", Document.class);
        this.nestedId = mapper.convert(nestedId, NestedId.class);
        this.data = document.get("data", String.class);
    }


    @Data
    public static class NestedId implements Mappable {
        private Long id;

        @Override
        public Document write(NitriteMapper mapper) {
            return Document.createDocument()
                .put("id", id);
        }

        @Override
        public void read(NitriteMapper mapper, Document document) {
            id = document.get("id", Long.class);
        }
    }
}
