package org.dizitart.no2.benchmark.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class Address {
    @Getter @Setter
    private String street;
    @Getter @Setter
    private String zip;
}
