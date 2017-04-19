package org.dizitart.no2.objects;

import lombok.experimental.UtilityClass;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.NitriteContext;

/**
 * A factory class to open a {@link ObjectRepository}.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
@UtilityClass
public class RepositoryFactory {

    /**
     * Opens an object repository for a specific `type`.
     *
     * @param <T>               the type of the object to store
     * @param type              the type
     * @param collection        the underlying {@link NitriteCollection}
     * @param nitriteContext    the nitrite context
     * @return the object repository
     */
    public static <T> ObjectRepository<T> open(Class<T> type,
                                               NitriteCollection collection,
                                               NitriteContext nitriteContext) {
        return new DefaultObjectRepository<>(type, collection, nitriteContext);
    }
}
