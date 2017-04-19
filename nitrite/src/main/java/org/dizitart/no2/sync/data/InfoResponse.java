package org.dizitart.no2.sync.data;

import lombok.Data;

/**
 * The DataGate server info operation response.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
@Data
public class InfoResponse {

    /**
     * The server vendor name.
     *
     * @param vendor the vendor name
     * @return the vendor name.
     * */
    private String vendor;

    /**
     * The DataGate server version.
     *
     * @param version the server version
     * @return the server version.
     * */
    private String version;

    /**
     * The DataGate server storage details.
     *
     * @param storage the server storage details
     * @return the server storage details.
     * */
    private Storage storage;

    /**
     * The DataGate server platform details.
     *
     * @param platform the server platform details
     * @return the server platform details.
     * */
    private Platform platform;

    /**
     * Represents server storage details.
     *
     * @since 1.0
     * @author Anindya Chatterjee
     */
    @Data
    public static class Storage {

        /**
         * The storage vendor name.
         *
         * @param vendor the vendor name
         * @return the vendor name.
         * */
        private String vendor;

        /**
         * The storage version.
         *
         * @param version the storage version
         * @return the storage version.
         * */
        private String version;
    }

    /**
     * Represents server platform details.
     *
     * @since 1.0
     * @author Anindya Chatterjee
     */
    @Data
    public static class Platform {

        /**
         * The server os name.
         *
         * @param os the os name
         * @return the os name.
         * */
        private String os;

        /**
         * The platform architecture.
         *
         * @param arch the platform architecture
         * @return the platform architecture.
         * */
        private String arch;

        /**
         * The java runtime details.
         *
         * @param java the java runtime details
         * @return the java runtime details.
         * */
        private String java;
    }
}
