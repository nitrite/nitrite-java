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

package org.dizitart.no2.sync.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dizitart.no2.collection.Document;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {
    private Set<String> added = new HashSet<>();
    private Set<String> removed = new HashSet<>();

    @SuppressWarnings("unchecked")
    public static Receipt fromDocument(Document document) {
        return new Receipt(
            (Set<String>) document.get("added", Set.class),
            (Set<String>) document.get("removed", Set.class)
        );
    }

    public Document toDocument() {
        return Document.createDocument("added", added)
            .put("removed", removed);
    }
}
