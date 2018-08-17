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

package org.dizitart.no2.sync.types;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.dizitart.no2.Document;

import java.io.Serializable;
import java.util.List;

/**
 * The DataGate server fetch operation response.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Data
@AllArgsConstructor
public class FetchResponse implements Serializable {
    private static final long serialVersionUID = 1487242605L;

    /**
     * The list of {@link Document}s fetched from the server.
     *
     * @param documents the list of {@link Document}s
     * @return the list of {@link Document}s fetched from server.
     * */
    private List<Document> documents;

    /**
     * Instantiates a new {@link FetchResponse}.
     */
    public FetchResponse() {}
}
