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

package org.dizitart.no2.sync.module;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.dizitart.no2.collection.Document;

import java.io.IOException;
import java.util.Map;

/**
 * @author Anindya Chatterjee
 */
public class DocumentDeserializer extends JsonDeserializer<Document> {

    @Override
    public Document deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Map<String, Object> map = p.readValueAs(new TypeReference<Map<String, Object>>() {
        });
        return toDocument(map);
    }

    @SuppressWarnings("unchecked")
    private Document toDocument(Map<String, Object> map) {
        Document document = Document.createDocument();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                value = toDocument((Map<String, Object>) value);
            }
            document.put(entry.getKey(), value);
        }
        return document;
    }
}
