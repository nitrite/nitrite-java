package org.dizitart.no2.common;

import lombok.Data;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.util.StringUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.dizitart.no2.common.Constants.INTERNAL_NAME_SEPARATOR;
import static org.dizitart.no2.common.util.ValidationUtils.notEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee
 */
@Data
public class Fields implements Comparable<Fields>, Serializable {
    private static final long serialVersionUID = 1601646404L;

    // order of the given fields matter
    private List<Pair<String, SortOrder>> fieldList;
    private transient List<String> fieldNames;

    private Fields() {
        fieldList = new ArrayList<>();
    }

    public static Fields single(String field) {
        Fields fields = new Fields();
        fields.fieldList.add(new Pair<>(field, SortOrder.Ascending));
        return fields;
    }

    @SafeVarargs
    public static Fields multiple(Pair<String, SortOrder>... fields) {
        notNull(fields, "fields cannot be null");
        notEmpty(fields, "fields cannot be empty");

        Fields f = new Fields();
        f.fieldList.addAll(Arrays.asList(fields));
        return f;
    }

    public List<String> getFieldNames() {
        if (fieldNames != null) {
            return fieldNames;
        }

        fieldNames = new ArrayList<>();
        for (Pair<String, SortOrder> pair : fieldList) {
            fieldNames.add(pair.getFirst());
        }
        return fieldNames;
    }

    public Pair<String, SortOrder> getFirstKey() {
        return fieldList.get(0);
    }

    public SortOrder getSortOrder(String fieldName) {
        for (Pair<String, SortOrder> pair : fieldList) {
            if (pair.getFirst().equals(fieldName)) {
                return pair.getSecond();
            }
        }
        return null;
    }

    public String getEncodedName() {
        return StringUtils.join(INTERNAL_NAME_SEPARATOR, getFieldNames());
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("[");
        int count = 0;
        for (Pair<String, SortOrder> field : fieldList) {
            count++;
            stringBuilder.append("{")
                .append(field.getFirst())
                .append(": ")
                .append(field.getSecond())
                .append("}");

                if (count != fieldList.size()) {
                    stringBuilder.append(", ");
                }
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @Override
    public int compareTo(Fields other) {
        if (other == null) return 1;
        int fieldsSize = getFieldNames().size();
        int otherFieldsSize = other.getFieldNames().size();
        int result = Integer.compare(fieldsSize, otherFieldsSize);
        if (result == 0) {
            String[] keys = getFieldNames().toArray(new String[0]);
            String[] otherKeys = other.getFieldNames().toArray(new String[0]);
            for (int i = 0; i < keys.length; i++) {
                int cmp = keys[i].compareTo(otherKeys[i]);
                if (cmp != 0) {
                    return cmp;
                }
            }
        }

        return result;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(fieldList);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        fieldList = (List<Pair<String, SortOrder>>) stream.readObject();
    }
}
