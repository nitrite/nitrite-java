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

package org.dizitart.no2.index;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.dizitart.no2.common.Fields;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static org.dizitart.no2.common.util.ValidationUtils.notEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * Represents a nitrite database index.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IndexDescriptor implements Comparable<IndexDescriptor>, Serializable {
    private static final long serialVersionUID = 1576690829L;

    /**
     * Specifies the type of the index.
     *
     * @return the type of the index.
     * @see IndexType
     */
    @Getter
    private String indexType;

    /**
     * Gets the target fields for the index.
     *
     * @return the target fields.
     */
    @Getter
    private Fields indexFields;

    /**
     * Gets the collection name.
     *
     * @return the collection name.
     */
    @Getter
    private String collectionName;

    /**
     * Instantiates a new Index.
     *
     * @param indexType      the index type
     * @param fields          the value
     * @param collectionName the collection name
     */
    public IndexDescriptor(String indexType, Fields fields, String collectionName) {
        notNull(indexType, "indexType cannot be null");
        notNull(fields, "fields cannot be null");
        notNull(collectionName, "collectionName cannot be null");
        notEmpty(collectionName, "collectionName cannot be empty");

        this.indexType = indexType;
        this.indexFields = fields;
        this.collectionName = collectionName;
    }

    @Override
    public int compareTo(IndexDescriptor other) {
        if (other == null) return 1;

        // compound index have highest cardinality
        if (this.isCompoundIndex() && !other.isCompoundIndex()) return 1;

        // unique index has the next highest cardinality
        if (this.isUniqueIndex() && !other.isUniqueIndex()) return 1;

        // for two unique indices, the one with encompassing higher
        // number of fields has the higher cardinality
        if (this.isUniqueIndex()) {
            return this.indexFields.compareTo(other.indexFields);
        }

        // for two non-unique indices, the one with encompassing higher
        // number of fields has the higher cardinality
        if (!other.isUniqueIndex()) {
            return this.indexFields.compareTo(other.indexFields);
        }

        return -1;
    }

    public boolean isCompoundIndex() {
        return indexFields.getFieldNames().size() > 1;
    }

    private boolean isUniqueIndex() {
        return indexType.equals(IndexType.Unique);
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeUTF(indexType);
        stream.writeObject(indexFields);
        stream.writeUTF(collectionName);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        indexType = stream.readUTF();
        indexFields = (Fields) stream.readObject();
        collectionName = stream.readUTF();
    }
}
