/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.sync.types;

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
