package org.dizitart.no2.migration;

/**
 * Represents a type converter.
 *
 * @param <S> the type parameter
 * @param <T> the type parameter
 * @author Anindya Chatterjee
 * @since 4.0
 */
public interface TypeConverter<S, T> {
    /**
     * Converts an object of type <code>S</code> to an object of type <code>T</code>.
     *
     * @param source the source
     * @return the target object
     */
    T convert(S source);
}
