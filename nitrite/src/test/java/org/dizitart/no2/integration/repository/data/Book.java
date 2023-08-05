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
import org.dizitart.no2.common.mapper.EntityConverter;
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
    @Index(fields = "tags", type = IndexType.NON_UNIQUE),
    @Index(fields = "description", type = IndexType.FULL_TEXT),
    @Index(fields = { "price", "publisher" })
})
public class Book {
    @Id(fieldName = "book_id", embeddedFields = { "isbn", "book_name" })
    private BookId bookId;

    private String publisher;

    private Double price;

    private List<String> tags;

    private String description;

    public static class BookConverter implements EntityConverter<Book> {

        @Override
        public Class<Book> getEntityType() {
            return Book.class;
        }

        @Override
        public Document toDocument(Book entity, NitriteMapper nitriteMapper) {
            return createDocument("book_id", nitriteMapper.tryConvert(entity.bookId, Document.class))
                .put("publisher", entity.publisher)
                .put("price", entity.price)
                .put("tags", entity.tags)
                .put("description", entity.description);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Book fromDocument(Document document, NitriteMapper nitriteMapper) {
            Book entity = new Book();
            entity.bookId = (BookId) nitriteMapper.tryConvert(document.get("book_id"), BookId.class);
            entity.publisher = document.get("publisher", String.class);
            entity.price = document.get("price", Double.class);
            entity.tags = (List<String>) document.get("tags", List.class);
            entity.description = document.get("description", String.class);
            return entity;
        }
    }
}
