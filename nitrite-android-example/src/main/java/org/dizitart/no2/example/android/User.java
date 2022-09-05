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

package org.dizitart.no2.example.android;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;

/**
 * @author Anindya Chatterjee.
 */
public class User {
    private String id;
    private String username;
    private String email;

    // needed for deserialization
    public User() {
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.id = "" + System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public static class Converter implements EntityConverter<User> {

        @Override
        public Class<User> getEntityType() {
            return User.class;
        }

        @Override
        public Document toDocument(User entity, NitriteMapper nitriteMapper) {
            Document document = Document.createDocument();
            document.put("id", entity.id);
            document.put("username", entity.username);
            document.put("email", entity.email);
            return document;
        }

        @Override
        public User fromDocument(Document document, NitriteMapper nitriteMapper) {
            User entity = new User();
            entity.id = (String) document.get("id");
            entity.username = (String) document.get("username");
            entity.email = (String) document.get("email");
            return entity;
        }
    }
}
