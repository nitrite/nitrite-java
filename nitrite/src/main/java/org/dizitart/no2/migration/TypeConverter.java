package org.dizitart.no2.migration;

/**
 * @author Anindya Chatterjee
 */
public interface TypeConverter<S, T> {
    T convert(S source);
}
