/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.filters;

import lombok.Data;
import org.dizitart.no2.exceptions.ValidationException;

/**
 * @author Anindya Chatterjee
 */
class BetweenFilter<T> extends AndFilter {

    public BetweenFilter(String field, Bound<T> bound) {
        super(getRhs(field, bound), getLhs(field, bound));
    }

    private static <T> Filter getRhs(String field, Bound<T> bound) {
        validateBound(bound);
        T value = bound.upperBound;
        if (bound.upperInclusive) {
            return new LesserEqualFilter(field, (Comparable<?>) value);
        } else {
            return new LesserThanFilter(field, (Comparable<?>) value);
        }
    }

    private static <T> Filter getLhs(String field, Bound<T> bound) {
        validateBound(bound);
        T value = bound.lowerBound;
        if (bound.lowerInclusive) {
            return new GreaterEqualFilter(field, (Comparable<?>) value);
        } else {
            return new GreaterThanFilter(field, (Comparable<?>) value);
        }
    }

    private static <T> void validateBound(Bound<T> bound) {
        if (bound == null) {
            throw new ValidationException("bound cannot be null");
        }

        if (!(bound.upperBound instanceof Comparable) || !(bound.lowerBound instanceof Comparable)) {
            throw new ValidationException("upper bound or lower bound value must be comparable");
        }
    }


    @Data
    public static class Bound<T> {
        private T upperBound;
        private T lowerBound;
        private boolean upperInclusive;
        private boolean lowerInclusive;

        public Bound(T lowerBound, T upperBound) {
            this(lowerBound, upperBound, true);
        }

        public Bound(T lowerBound, T upperBound, boolean inclusive) {
            this(lowerBound, upperBound, inclusive, inclusive);
        }

        public Bound(T lowerBound, T upperBound, boolean lowerInclusive, boolean upperInclusive) {
            this.upperBound = upperBound;
            this.lowerBound = lowerBound;
            this.upperInclusive = upperInclusive;
            this.lowerInclusive = lowerInclusive;
        }
    }
}
