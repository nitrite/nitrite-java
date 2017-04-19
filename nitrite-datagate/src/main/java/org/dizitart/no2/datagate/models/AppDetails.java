package org.dizitart.no2.datagate.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author Anindya Chatterjee.
 */
@Data
public class AppDetails {
    private String name;
    private List<AppVersion> versionList;

    @Data
    @AllArgsConstructor
    public static class AppVersion {
        private String number;
        private String usage;
        private float percentage;
    }
}
