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

package org.dizitart.no2.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Represents a key and a value pair.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
@Data
@EqualsAndHashCode
@AllArgsConstructor
public class KeyValuePair<Key, Value> implements Serializable {

    /**
     * The key of the pair.
     *
     * @param key the key to set.
     * @returns the key.
     */
    private Key key;

    /**
     * The value of the pair.
     *
     * @param value the value to set.
     * @returns the value.
     */
    private Value value;

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(key);
        stream.writeObject(value);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        key = (Key) stream.readObject();
        value = (Value) stream.readObject();
    }
}
