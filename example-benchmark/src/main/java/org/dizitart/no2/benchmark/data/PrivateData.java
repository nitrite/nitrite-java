package org.dizitart.no2.benchmark.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class PrivateData {
    @Getter @Setter
    private String username;
    @Getter @Setter
    private String password;
}
