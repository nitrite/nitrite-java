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
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.annotations.Entity;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;

import java.util.List;

import static org.dizitart.no2.collection.Document.createDocument;

/**
 * @author Anindya Chatterjee
 */
@Data
@Entity(value = "books", indices = {
    @Index(value = "tags", type = IndexType.NonUnique),
    @Index(value = "description", type = IndexType.Fulltext),
    @Index(value = { "price", "publisher" })
})
public class Book implements Mappable {
    @Id(fieldName = "book_id")
    private BookId bookId;

    private String publisher;

    private Double price;

    private List<String> tags;

    private String description;

    @Override
    public Document write(NitriteMapper mapper) {
        return createDocument("book_id", mapper.convert(bookId, Document.class))
            .put("publisher", publisher)
            .put("price", price)
            .put("tags", tags)
            .put("description", description);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(NitriteMapper mapper, Document document) {
        bookId = mapper.convert(document.get("book_id"), BookId.class);
        publisher = document.get("publisher", String.class);
        price = document.get("price", Double.class);
        tags = (List<String>) document.get("tags", List.class);
        description = document.get("description", String.class);
    }
}
