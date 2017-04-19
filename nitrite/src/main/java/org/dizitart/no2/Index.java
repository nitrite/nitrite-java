package org.dizitart.no2;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.dizitart.no2.util.IndexUtils.internalName;
import static org.dizitart.no2.util.ValidationUtils.notEmpty;
import static org.dizitart.no2.util.ValidationUtils.notNull;

/**
 * Represents a nitrite database index.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 * @see NitriteCollection#createIndex(String, IndexOptions)
 */
@EqualsAndHashCode
@ToString
public class Index implements Comparable<Index>, Serializable {

    /**
     * Specifies the type of the index.
     *
     * @return the type of the index.
     * @see IndexType
     * */
    @Getter
    private IndexType indexType;

    /**
     * Gets the target value for the index.
     *
     * @return the target value.
     * */
    @Getter
    private String field;

    /**
     * Gets the collection name.
     *
     * @return the collection name.
     * */
    @Getter
    private String collectionName;

    /**
     * Instantiates a new Index.
     *
     * @param indexType      the index type
     * @param field          the value
     * @param collectionName the collection name
     */
    public Index(IndexType indexType, String field, String collectionName) {
        notNull(indexType, errorMessage("indexType can not be null", VE_INDEX_NULL_INDEX_TYPE));
        notNull(field, errorMessage("field can not be null", VE_INDEX_NULL_FIELD));
        notEmpty(field, errorMessage("field can not be empty", VE_INDEX_EMPTY_FIELD));
        notNull(collectionName, errorMessage("collectionName can not be null", VE_INDEX_NULL_COLLECTION));
        notEmpty(collectionName, errorMessage("collectionName can not be empty", VE_INDEX_EMPTY_COLLECTION));

        this.indexType = indexType;
        this.field = field;
        this.collectionName = collectionName;
    }

    private Index() {
    }

    @Override
    public int compareTo(Index other) {
        return internalName(this).compareTo(internalName(other));
    }
}
