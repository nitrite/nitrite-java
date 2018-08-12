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

package org.dizitart.no2.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dizitart.no2.Document;

import java.io.Serializable;

/**
 * Represents affected item during collection modification.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
@ToString
@Getter @Setter
public class ChangedItem implements Serializable {
    /**
     * Specifies the changed document.
     *
     * @param document the document.
     * @returns the document.
     * */
    private Document document;

    /**
     * Specifies the change type.
     *
     * @param changeType the type of the change.
     * @returns the type of the change.
     * */
    private ChangeType changeType;

    /**
     * Specifies the unix timestamp of the change.
     *
     * @param changeTimestamp the unix timestamp of the change.
     * @returns the unix timestamp of the change.
     * */
    private long changeTimestamp;
}
