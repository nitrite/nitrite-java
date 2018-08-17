/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.collection.objects.data;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Anindya Chatterjee.
 */
@Getter
@Setter
public class WithOutId implements Comparable<WithOutId> {
    private String name;
    private long number;

    @Override
    public int compareTo(WithOutId o) {
        return Long.compare(number, o.number);
    }
}
