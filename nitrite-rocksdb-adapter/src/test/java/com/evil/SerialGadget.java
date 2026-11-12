package com.evil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/** Test gadget whose readObject records that native deserialization ran. */
public class SerialGadget implements Serializable, Comparable<SerialGadget> {
    public static volatile boolean deserialized;

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        deserialized = true;
    }

    @Override
    public int compareTo(SerialGadget o) {
        return 0;
    }
}
