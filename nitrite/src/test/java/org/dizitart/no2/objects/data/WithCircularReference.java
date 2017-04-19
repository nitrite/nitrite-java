package org.dizitart.no2.objects.data;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Anindya Chatterjee.
 */
@Getter
@Setter
public class WithCircularReference {
    private String name;
    private WithCircularReference parent;
}
