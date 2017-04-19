package org.dizitart.no2.meta;

/**
 * Interface to be implemented by database objects that wish to be
 * aware of their meta data.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
public interface MetadataAware {
    /**
     * Returns the meta data attributes of an object.
     *
     * @return the meta data attributes.
     * */
    Attributes getAttributes();

    /**
     * Sets new meta data attributes.
     *
     * @param attributes new meta data attributes.
     * */
    void setAttributes(Attributes attributes);
}
