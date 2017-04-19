package org.dizitart.no2.filters;

import lombok.Getter;

/**
 * @author Anindya Chatterjee.
 */
@Getter
abstract class StringFilter extends BaseFilter {
    String field;
    String value;

    StringFilter(String field, String value) {
        this.field = field;
        this.value = value;
    }
}
