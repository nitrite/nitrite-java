package org.dizitart.no2.objects.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author Anindya Chatterjee
 */
@Getter @Setter
@EqualsAndHashCode
public class WithDateId {
    private Date id;
    private String name;
}
