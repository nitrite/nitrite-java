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

package org.dizitart.no2.mapper.jackson.integration.repository.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Anindya Chatterjee.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Index(fields = "companyName")
public class Company implements Serializable {
    @Id(fieldName = "company_id")
    @JsonProperty("company_id")
    private Long companyId;
    private String companyName;
    private Date dateCreated;
    private List<String> departments;
    private Map<String, List<Employee>> employeeRecord;
}
