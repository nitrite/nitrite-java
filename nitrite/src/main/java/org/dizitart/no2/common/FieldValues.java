package org.dizitart.no2.common;

import lombok.Data;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a {@link Fields} and their corresponding values from a document.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
@Data
public class FieldValues {
    private NitriteId nitriteId;
    private Fields fields;
    private List<Pair<String, Object>> values;

    /**
     * Instantiates a new Field values.
     */
    public FieldValues() {
        values = new ArrayList<>();
    }

    /**
     * Get the value of the field.
     *
     * @param field the field
     * @return the value
     */
    public Object get(String field) {
        if (fields.getFieldNames().contains(field)) {
            for (Pair<String, Object> value : values) {
                if (value.getFirst().equals(field)) {
                    return value.getSecond();
                }
            }
        }
        return null;
    }

    /**
     * Gets the {@link Fields}.
     *
     * @return the fields
     */
    public Fields getFields() {
        if (fields != null) {
            return fields;
        }

        this.fields = new Fields();
        fields.setFieldNames(new ArrayList<>());
        for (Pair<String, Object> value : getValues()) {
            fields.getFieldNames().add(value.getFirst());
        }
        return fields;
    }
}
