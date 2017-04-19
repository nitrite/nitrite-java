package org.dizitart.no2.datagate.models;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
@AllArgsConstructor
public class Device {
    private String legend;
    private String hoverColor;
    private String color;
    private String name;
    private int usage;
}
