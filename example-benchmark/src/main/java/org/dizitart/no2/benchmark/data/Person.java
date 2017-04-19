package org.dizitart.no2.benchmark.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class Person {
    @Getter @Setter
    private String firstName;
    @Getter @Setter
    private String lastName;
    @Getter @Setter
    private List<Address> addresses = new ArrayList<>();
    @Getter @Setter
    private Address defaultAddress;
    @Getter @Setter
    private PrivateData privateData;
    @Getter @Setter
    private String personalNote;
}
