package org.dizitart.no2.migration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Anindya Chatterjee
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VersionInfo {
    private String startVersion;
    private String endVersion;
}
