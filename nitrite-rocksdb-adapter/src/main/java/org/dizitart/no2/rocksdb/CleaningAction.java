package org.dizitart.no2.rocksdb;

/**
 * @since 4.0
 * @author Anindya Chatterjee
 */
class CleaningAction implements Runnable {

    private final AutoCloseable resource;

    CleaningAction(AutoCloseable resource) {
        this.resource = resource;
    }

    @Override
    public void run() {
        try {
            resource.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
