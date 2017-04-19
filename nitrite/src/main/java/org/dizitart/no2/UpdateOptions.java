package org.dizitart.no2;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Settings to control update operation in {@link NitriteCollection}.
 *
 * @see NitriteCollection#update(Filter, Document, UpdateOptions)
 * @author Anindya Chatterjee
 * @since 1.0
 */
@ToString
public class UpdateOptions {

    /**
     * Indicates if the update operation will insert a new document if it
     * does not find any existing document to update.
     *
     * @param upsert a value indicating, if a new document to insert in case the
     *               filter fails to find a document to update.
     * @return `true` if a new document to insert; otherwise, `false`.
     * @see NitriteCollection#update(Filter, Document, UpdateOptions)
     * */
    @Getter @Setter
    private boolean upsert;

    /**
     * Indicates if only one document will be updated or all of them.
     *
     * @param justOne a value indicating if only one document to update or all.
     * @return `true` if only one document to update; otherwise, `false`.
     * */
    @Getter @Setter
    private boolean justOnce;

    /**
     * Creates a new {@link UpdateOptions}.
     *
     * @param upsert the upsert flag
     * @return the {@link UpdateOptions}.
     */
    public static UpdateOptions updateOptions(boolean upsert) {
        UpdateOptions options = new UpdateOptions();
        options.setUpsert(upsert);
        return options;
    }

    /**
     * Creates a new {@link UpdateOptions}.
     *
     * @param upsert   the upsert flag
     * @param justOnce the justOnce flag
     * @return the {@link UpdateOptions}.
     */
    public static UpdateOptions updateOptions(boolean upsert, boolean justOnce) {
        UpdateOptions options = new UpdateOptions();
        options.setUpsert(upsert);
        options.setJustOnce(justOnce);
        return options;
    }
}
