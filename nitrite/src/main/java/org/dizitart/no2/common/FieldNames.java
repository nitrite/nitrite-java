package org.dizitart.no2.common;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
@Getter @Setter
public class FieldNames {
    private Set<String> names;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldNames)) return false;
        FieldNames that = (FieldNames) o;
        return Objects.equals(getNames(), that.getNames());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNames());
    }
}
