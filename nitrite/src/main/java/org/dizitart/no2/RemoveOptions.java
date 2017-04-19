package org.dizitart.no2;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Settings to control remove operation in {@link NitriteCollection}.
 *
 * @see NitriteCollection#remove(Filter, RemoveOptions)
 * @author Anindya Chatterjee
 * @since 1.0
 */
@ToString
public class RemoveOptions {

    /**
     * Indicates if only one document will be removed or all of them.
     *
     * @param justOne a value indicating if only one document to remove or all.
     * @return `true` if only one document to remove; otherwise, `false`.
     * */
    @Getter @Setter
    private boolean justOne;
}
