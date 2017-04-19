package org.dizitart.no2.objects.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author Anindya Chatterjee.
 */
@EqualsAndHashCode
public class SubEmployee {
    @Getter
    @Setter
    private Long empId;

    @Getter
    @Setter
    private Date joinDate;

    @Getter
    @Setter
    private String address;
}
