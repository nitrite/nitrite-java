package com.evil;

import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Stands in for a third-party gadget class on the classpath for GHSA-q86x-r2q3-vf4p.
 */
public class EvilGadget implements Serializable {
    private static final long serialVersionUID = 1L;
    public static boolean readObjectCalled = false;

    private void readObject(ObjectInputStream in) throws Exception {
        in.defaultReadObject();
        readObjectCalled = true;
    }
}
