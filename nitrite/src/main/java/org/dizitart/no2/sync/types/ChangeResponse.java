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
 * The DataGate server change operation response.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Data
@AllArgsConstructor
public class ChangeResponse implements Serializable {
    private static final long serialVersionUID = 1487242652L;

    /**
     * The boolean value indicating whether the server
     * data updated successfully or not.
     *
     * @param changed the boolean value
     * @return `true` if change is successful; `false` otherwise.
     * */
    private boolean changed;

    /**
     * Instantiates a new {@link ChangeResponse}.
     */
    public ChangeResponse(){}
}
