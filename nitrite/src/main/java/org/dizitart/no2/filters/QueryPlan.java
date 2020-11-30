package org.dizitart.no2.filters;

import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
public class QueryPlan {
    private Filter indexedFilter;
    private Filter nonIndexedFilter;
}
