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

package org.dizitart.no2.datagate.models;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.Document;

/**
 * A wrapper object for nitrite documents to store in
 * remote store.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Data
public class NitriteDocument {
    public static final String DELETE_TIME = "deleteTime";
    public static final String SYNC_TIME = "syncTime";
    public static final String DELETED = "deleted";

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private long _id;
    private Document document;
    private boolean deleted;
    private long deleteTime;
    private long syncTime;

    public long getId() {
        return _id;
    }

    public void setId(long id) {
        this._id = id;
    }
}
