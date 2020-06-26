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
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;

import java.io.Serializable;

/**
 * @author Anindya Chatterjee.
 */
@EqualsAndHashCode
public class Note implements Serializable, Mappable {
    @Getter
    @Setter
    private Long noteId;
    @Getter
    @Setter
    private String text;

    @Override
    public Document write(NitriteMapper mapper) {
        return Document.createDocument().put("noteId", noteId).put("text", text);
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        noteId = document.get("noteId", Long.class);
        text = document.get("text", String.class);
    }
}
