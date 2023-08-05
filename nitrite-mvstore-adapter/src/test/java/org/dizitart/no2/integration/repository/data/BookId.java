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

import static org.dizitart.no2.collection.Document.createDocument;

/**
 * @author Anindya Chatterjee
 */
@Data
public class BookId {
    private String isbn;

    private String name;

    private String author;

    public static class BookIdConverter implements EntityConverter<BookId> {

        @Override
        public Class<BookId> getEntityType() {
            return BookId.class;
        }

        @Override
        public Document toDocument(BookId entity, NitriteMapper nitriteMapper) {
            return createDocument("isbn", entity.isbn)
                .put("book_name", entity.name)
                .put("author", entity.author);
        }

        @Override
        public BookId fromDocument(Document document, NitriteMapper nitriteMapper) {
            BookId entity = new BookId();
            entity.isbn = document.get("isbn", String.class);
            entity.name = document.get("book_name", String.class);
            entity.author = document.get("author", String.class);
            return entity;
        }
    }
}
