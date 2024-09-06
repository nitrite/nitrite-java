/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.mapper.jackson.integration.migrate;

import lombok.Data;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.annotations.Entity;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;

/**
 * @author Anindya Chatterjee
 */
@Data
@Entity(value = "new", indices = {
    @Index(fields = "familyName", type = IndexType.NON_UNIQUE),
    @Index(fields = "fullName", type = IndexType.NON_UNIQUE),
    @Index(fields = "literature.ratings", type = IndexType.NON_UNIQUE),
})
public class NewClass {
    @Id
    private Long empId;
    private String firstName;
    private String familyName;
    private String fullName;
    private Literature literature;


    @Data
    public static class Literature {
        private String text;
        private Integer ratings;
    }
}
