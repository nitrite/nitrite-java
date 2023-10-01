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

package org.dizitart.no2.common.tuples;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**
 * A simple generic class representing a pair of values.
 *
 * @param <A> the type of the first value in the pair
 * @param <B> the type of the second value in the pair
 * @author Anindya Chatterjee.
 * @since 4.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pair<A, B> implements Serializable {
    private static final long serialVersionUID = 1598774244L;

    private A first;
    private B second;

    /**
     * Creates a new pair.
     *
     * @param <A>    the type parameter
     * @param <B>    the type parameter
     * @param first  the first
     * @param second the second
     * @return the pair
     */
    public static <A, B> Pair<A, B> pair(A first, B second) {
        return new Pair<>(first, second);
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(first);
        stream.writeObject(second);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        first = (A) stream.readObject();
        second = (B) stream.readObject();
    }
}
