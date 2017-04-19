package org.dizitart.no2.objects.data;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Anindya Chatterjee.
 */
@Getter @Setter
public class StressRecord {
    private String firstName;
    private boolean processed;
    private String lastName;
    private boolean failed;
    private String notes;
}
