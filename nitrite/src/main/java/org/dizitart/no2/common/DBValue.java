/*
 * Copyright (c) 2017-2022 Nitrite author or authors.
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

package org.dizitart.no2.common;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.dizitart.no2.common.util.Comparables;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author Anindya Chatterjee
 * @since 4.0
 */
@Data
public class DBValue implements Comparable<DBValue>, Serializable {
    private static final long serialVersionUID = 1617440702L;

    @Setter(AccessLevel.PRIVATE)
    private Comparable<?> value;

    private DBValue() {
    }

    public DBValue(Comparable<?> value) {
        this.value = value;
    }

    @Override
    public int compareTo(DBValue o) {
        if (o == null || o.value == null) {
            return 1;
        }

        if (value == null) {
            return -1;
        }

        return Comparables.compare(value, o.value);
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(value);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        this.value = (Comparable<?>) stream.readObject();
    }
}
