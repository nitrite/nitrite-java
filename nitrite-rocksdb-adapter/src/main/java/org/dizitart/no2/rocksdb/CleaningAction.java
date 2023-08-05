package org.dizitart.no2.rocksdb;

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
