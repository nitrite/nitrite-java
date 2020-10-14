package org.dizitart.no2.common;

import lombok.Data;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.ValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anindya Chatterjee
 */
@Data
public class FieldValues {
    private NitriteId nitriteId;
    private Fields fields;
    private List<Pair<String, Object>> values;

    public FieldValues() {
        values = new ArrayList<>();
    }

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

    public Object getFirstValue() {
        return getValueAt(0);
    }

    public Object getValueAt(int index) {
        if (index > values.size() - 1) {
            throw new ValidationException("invalid index provided");
        }

        return values.get(index).getSecond();
    }
}
