package org.dizitart.no2.datagate.models;

import lombok.Data;

import java.io.Serializable;

/**
 * A generic Data Gate server error message response.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Data
public class DataGateError implements Serializable {
    private static final long serialVersionUID = 1487242730L;
    private String message;

    public DataGateError() {}
}
