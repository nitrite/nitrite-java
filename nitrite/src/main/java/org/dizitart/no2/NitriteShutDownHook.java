package org.dizitart.no2;

/**
 * A JVM shutdown hook to close database properly before exiting for good.
 *
 * @author Anindya Chatterjee.
 */
class NitriteShutDownHook extends Thread {
    private Nitrite db;

    NitriteShutDownHook(Nitrite db) {
        this.db = db;
    }

    @Override
    public void run() {
        if (db != null && !db.isClosed()) {
            // close the db immediately and discards
            // any unsaved changes to avoid corruption
            db.closeImmediately();
        }
    }
}
