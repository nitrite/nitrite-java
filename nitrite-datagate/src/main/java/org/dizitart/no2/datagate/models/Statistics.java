package org.dizitart.no2.datagate.models;

import lombok.Data;

import java.util.List;

/**
 * @author Anindya Chatterjee.
 */
@Data
public class Statistics {
    private int userCount;
    private int clientCount;
    private int collectionCount;
    private long documentCount;
    private Long[][] syncGraphData;
    private List<Device> deviceList;
    private List<AppDetails> appList;
}
