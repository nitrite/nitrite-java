package org.dizitart.no2.objects.data;

import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
public class ElemMatch {
    private long id;
    private String[] strArray;
    private ProductScore[] productScores;
}
