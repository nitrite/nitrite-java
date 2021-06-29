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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.annotations.Entity;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;

import java.util.List;

/**
 * @author Anindya Chatterjee
 */
@Data
@Entity(value = "books", indices = {
    @Index(value = "tags", type = IndexType.NON_UNIQUE),
    @Index(value = "description", type = IndexType.FULL_TEXT),
    @Index(value = { "price", "publisher" })
})
public class Book {
    @JsonProperty("book_id")
    @Id(fieldName = "book_id")
    private BookId bookId;

    private String publisher;

    private Double price;

    private List<String> tags;

    private String description;
}
