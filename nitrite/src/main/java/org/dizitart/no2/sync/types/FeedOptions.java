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

package org.dizitart.no2.sync.types;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * Represents the options to extract the change
 * feeds from server.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Data
@ToString
public class FeedOptions implements Serializable {
    private static final long serialVersionUID = 1486043735L;

    /**
     * The from sequence number. It is generally the last sync
     * timestamp stored in local.
     *
     * @param fromSequence the from sequence number
     * @return the from sequence number.
     * */
    private Long fromSequence;

    /**
     * The offset from which the change feed needs to
     * be fetched from server.
     *
     * @param offset the feed fetch offset
     * @return the change feed fetch offset value.
     * */
    private int offset;

    /**
     * The limit up to which the change feed needs to be
     * fetched from the offset.
     *
     * @param limit the feed fetch limit
     * @return the change feed fetch limit value.
     * */
    private int limit;

    /**
     * The boolean value indicating whether pagination should
     * required or not.
     *
     * @param paginated the boolean value
     * @return `true` if pagination is required; `false` otherwise.
     * */
    private boolean paginated;
}
