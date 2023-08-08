package org.dizitart.no2.support;

import com.fasterxml.jackson.core.JsonFactory;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents import options.
 *
 * @author Anindya Chatterjee
 * @see Importer
 * @since 4.0
 */
@Getter
@Setter
public class ImportOptions {
    /**
     * Specifies a {@link NitriteFactory} to create a {@link org.dizitart.no2.Nitrite} instance. This
     * instance will be used to import the collections and data.
     * <p>
     * This is a mandatory field. If not specified, the import operation will fail.
     * The {@link NitriteFactory} instance must be able to create a {@link org.dizitart.no2.Nitrite}, so
     * the database must not be open elsewhere. Upon completion of the import operation, the
     * {@link org.dizitart.no2.Nitrite} instance will be closed.
     *
     * @param nitriteFactory the nitriteFactory.
     * @return the nitriteFactory.
     */
    private NitriteFactory nitriteFactory;

    /**
     * Specifies a {@link JsonFactory} to create a {@link com.fasterxml.jackson.core.JsonGenerator} instance.
     * This instance will be used to read the exported data from a file.
     * <p>
     * This is an optional field. If not specified, a default one will be created.
     *
     * @param jsonFactory the jsonFactory.
     * @return the jsonFactory.
     */
    private JsonFactory jsonFactory;
}
