package org.dizitart.no2.common;

import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.index.IndexDescriptor;

import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
@Getter @Setter
public class IndexedFieldNames extends FieldNames {
    private Set<IndexDescriptor> supportedIndices;

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
