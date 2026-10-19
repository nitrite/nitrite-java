package com.evil;

import java.io.Serializable;

/**
 * Stands in for a third-party class that a crafted RocksDB file names for kryo to instantiate.
 */
public class KryoGadget implements Serializable {
    public static boolean instantiated = false;
    public String payload;

    public KryoGadget() {
        instantiated = true;
    }
}
