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

package org.dizitart.no2.repository.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;

@EqualsAndHashCode
@ToString
public class ClassC implements Mappable {
    @Getter
    @Setter
    private long id;
    @Getter
    @Setter
    private double digit;
    @Getter
    @Setter
    private ClassA parent;

    public static ClassC create(int seed) {
        ClassC classC = new ClassC();
        classC.id = seed * 5000;
        classC.digit = seed * 69.65;
        classC.parent = ClassA.create(seed);
        return classC;
    }

    @Override
    public Document write(NitriteMapper mapper) {
        return Document.createDocument()
            .put("id", id)
            .put("digit", digit)
            .put("parent", parent != null ? parent.write(mapper) : null);
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        id = document.get("id", Long.class);
        digit = document.get("digit", Double.class);
        if (document.get("parent") != null) {
            parent = new ClassA();
            parent.read(mapper, document.get("parent", Document.class));
        }
    }
}
