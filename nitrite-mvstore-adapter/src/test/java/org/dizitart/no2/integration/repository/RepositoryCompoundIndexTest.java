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

import org.dizitart.no2.integration.repository.data.Book;
import org.dizitart.no2.integration.repository.data.BookId;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee
 */
public class RepositoryCompoundIndexTest extends BaseObjectRepositoryTest {

    @Test
    public void testFindById() {
        BookId bookId = new BookId();
        bookId.setAuthor("John Doe");
        bookId.setIsbn("123456");
        bookId.setName("Nitrite Database");

        Book book = new Book();
        book.setBookId(bookId);
        book.setDescription("Some random book description");
        book.setPrice(22.56);
        book.setPublisher("My Publisher House");
        book.setTags(Arrays.asList("database", "nosql"));

        bookRepository.insert(book);

        Book bookById = bookRepository.getById(bookId);
        assertEquals(bookById, book);
    }

    @Test
    public void testMultipleBooksWithSamePartialId() {
        BookId bookId1 = new BookId();
        bookId1.setAuthor("John Doe");
        bookId1.setIsbn("123456");
        bookId1.setName("Nitrite Database");

        Book book1 = new Book();
        book1.setBookId(bookId1);
        book1.setDescription("Some random book description");
        book1.setPrice(22.56);
        book1.setPublisher("My Publisher House");
        book1.setTags(Arrays.asList("database", "nosql"));

        bookRepository.insert(book1);

        BookId bookId2 = new BookId();
        bookId2.setAuthor("Jane Doe");
        bookId2.setIsbn("123456");
        bookId2.setName("Another Nitrite Book");

        Book book2 = new Book();
        book2.setBookId(bookId2);
        book2.setDescription("Another random book description");
        book2.setPrice(30.00);
        book2.setPublisher("Another Publisher House");
        book2.setTags(Arrays.asList("database", "nosql", "java"));

        bookRepository.insert(book2);

        Book fetchedBook1 = bookRepository.getById(bookId1);
        Book fetchedBook2 = bookRepository.getById(bookId2);

        assertEquals(fetchedBook1, book1);
        assertEquals(fetchedBook2, book2);
    }
}
