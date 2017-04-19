package org.dizitart.no2.objects.data;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Anindya Chatterjee.
 */
@Getter
@Setter
public class WithTransientField {
    private transient String name;
    private long number;
}
