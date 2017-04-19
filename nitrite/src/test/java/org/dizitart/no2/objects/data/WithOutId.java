package org.dizitart.no2.objects.data;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Anindya Chatterjee.
 */
@Getter
@Setter
public class WithOutId implements Comparable<WithOutId> {
    private String name;
    private long number;

    @Override
    public int compareTo(WithOutId o) {
        return Long.compare(number, o.number);
    }
}
