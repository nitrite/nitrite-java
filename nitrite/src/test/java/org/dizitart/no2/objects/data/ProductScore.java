package org.dizitart.no2.objects.data;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Anindya Chatterjee
 */
@Getter
@Setter
public class ProductScore {
    private String product;
    private int score;

    ProductScore() {
    }

    public ProductScore(String product, int score) {
        this.product = product;
        this.score = score;
    }
}
