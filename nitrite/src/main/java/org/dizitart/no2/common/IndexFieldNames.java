package org.dizitart.no2.common;

import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.index.IndexDescriptor;

/**
 * @author Anindya Chatterjee
 */
@Getter @Setter
public class IndexFieldNames extends FieldNames {
    private IndexDescriptor indexDescriptor;

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
