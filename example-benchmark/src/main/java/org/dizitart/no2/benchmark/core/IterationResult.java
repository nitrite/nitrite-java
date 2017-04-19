package org.dizitart.no2.benchmark.core;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Anindya Chatterjee.
 */
public class IterationResult {
    @Getter @Setter
    private long time;

    @Getter @Setter
    private long memory;

    @Getter
    private int iteration;

    public IterationResult(int iteration) {
        this.iteration = iteration;
    }
}
