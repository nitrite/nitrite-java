package org.dizitart.no2.ui;

import javafx.beans.property.SimpleStringProperty;

/**
 * @author Anindya Chatterjee.
 */
public class NitriteKey {
    private final Object key;
    private final SimpleStringProperty keyString;

    public NitriteKey(Object key, String keyString) {
        this.key = key;
        this.keyString = new SimpleStringProperty(keyString);
    }

    public Object getKey() {
        return key;
    }

    public SimpleStringProperty getKeyString() {
        return keyString;
    }
}
