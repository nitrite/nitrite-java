package org.dizitart.no2.objects.data;

import lombok.EqualsAndHashCode;

/**
 * @author Anindya Chatterjee.
 */
@EqualsAndHashCode
public class WithOutGetterSetter {
    private String name;
    private long number;

    public WithOutGetterSetter() {
        name = "test";
        number = 2;
    }
}
