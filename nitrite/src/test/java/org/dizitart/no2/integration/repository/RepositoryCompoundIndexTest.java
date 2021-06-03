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

package org.dizitart.no2.integration.repository;

import lombok.val;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.integration.repository.data.DataGenerator;
import org.dizitart.no2.repository.Cursor;
import org.dizitart.no2.integration.repository.data.Book;
import org.junit.Test;

/**
 * @author Anindya Chatterjee
 */
public class RepositoryCompoundIndexTest /*extends BaseObjectRepositoryTest*/ {
    private Nitrite db;

    @Test
    public void test() {
        NitriteBuilder nitriteBuilder = Nitrite.builder()
            .fieldSeparator(".");
        db = nitriteBuilder.openOrCreate();

        val bookRepository = db.getRepository(Book.class);

        for (int i = 0; i < 10; i++) {
            Book book = DataGenerator.randomBook();
            bookRepository.insert(book);
        }

        Cursor<Book> bookCursor = bookRepository.find();
        for (Book book : bookCursor) {
            System.out.println(book);
        }
    }
}
