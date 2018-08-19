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

package org.dizitart.no2.datagate;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dizitart.no2.collection.IndexType;
import org.dizitart.no2.index.annotations.Id;
import org.dizitart.no2.index.annotations.Index;
import org.dizitart.no2.index.annotations.Indices;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Anindya Chatterjee.
 */
@ToString
@EqualsAndHashCode
@Indices({
        @Index(value = "joinDate", type = IndexType.NonUnique),
        @Index(value = "address", type = IndexType.Fulltext)
})
public class Employee implements Serializable {
    @Id
    @Getter
    @Setter
    private Long empId;

    @Getter
    @Setter
    private Date joinDate;

    @Getter
    @Setter
    private String address;

    @Getter
    @Setter
    private transient List<Employee> subordinates;

    @Getter
    @Setter
    private byte[] blob;

    public Employee() {}
}
