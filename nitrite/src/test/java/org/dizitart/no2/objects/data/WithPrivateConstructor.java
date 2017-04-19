package org.dizitart.no2.objects.data;

import lombok.EqualsAndHashCode;

/**
 * @author Anindya Chatterjee.
 */
@EqualsAndHashCode
public class WithPrivateConstructor {
    private String name;
    private long number;

    private WithPrivateConstructor() {
        name = "test";
        number = 2;
    }

    public static WithPrivateConstructor create(final String name, final long number) {
        WithPrivateConstructor obj = new WithPrivateConstructor();
        obj.number = number;
        obj.name = name;
        return obj;
    }
}
