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

import java.io.Serializable;

/**
 * The DataGate server size operation response.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class SizeResponse implements Serializable {
    private static final long serialVersionUID = 1487242966L;

    /**
     * The size of the server collection.
     *
     * @param size the size of the server collection
     * @return the size of the sever collection.
     * */
    private long size;

    /**
     * Instantiates a new {@link SizeResponse}.
     */
    public SizeResponse(){}
}
