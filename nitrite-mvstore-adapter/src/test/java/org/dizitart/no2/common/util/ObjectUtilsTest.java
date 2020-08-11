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

package org.dizitart.no2.common.util;

import lombok.Data;
import org.dizitart.no2.repository.data.ChildClass;
import org.dizitart.no2.repository.data.Employee;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.dizitart.no2.common.util.ObjectUtils.newInstance;

/**
 * @author Anindya Chatterjee.
 */
public class ObjectUtilsTest {

    @Test
    public void testNewInstance() {
        EnclosingType type = newInstance(EnclosingType.class, true);
        System.out.println(type);
    }

    @Data
    private static class EnclosingType {
        private ChildClass childClass;
        private FieldType fieldType;
    }

    @Data
    private static class FieldType {
        private Employee employee;
        private LocalDateTime currentDate;
    }
}
