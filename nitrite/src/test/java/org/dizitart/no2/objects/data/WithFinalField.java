package org.dizitart.no2.objects.data;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Anindya Chatterjee.
 */
@Getter
public class WithFinalField {
    @Setter
    private String name;
    private final long number;

    public WithFinalField() {
        number = 2;
    }
}
