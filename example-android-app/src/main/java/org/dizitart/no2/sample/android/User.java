/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.sample.android;

import org.dizitart.no2.Document;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;

/**
 * @author Anindya Chatterjee.
 */
public class User implements Mappable {
    private String id;
    private String username;
    private String email;

    // needed for deserialization
    public User(){}

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.id = ""+System.currentTimeMillis();
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

    @Override
    public Document write(NitriteMapper mapper) {
        Document document = new Document();
        document.put("id", id);
        document.put("username", username);
        document.put("email", email);
        return document;
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        id = (String) document.get("id");
        username = (String) document.get("username");
        email = (String) document.get("email");
    }
}
