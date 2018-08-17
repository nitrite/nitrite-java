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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dizitart.no2.index.annotations.Id;
import org.dizitart.no2.index.annotations.Index;
import org.dizitart.no2.index.annotations.Indices;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Anindya Chatterjee.
 */
@ToString
@EqualsAndHashCode
@Indices({
        @Index(value = "companyName")
})
public class Company implements Serializable {
    @Id
    @Getter
    @Setter
    private Long companyId;

    @Getter
    @Setter
    private String companyName;

    @Getter
    @Setter
    private Date dateCreated;

    @Getter
    @Setter
    private List<String> departments;

    @Getter
    @Setter
    private Map<String, List<Employee>> employeeRecord;
}
