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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@EqualsAndHashCode
@ToString
public class ClassA {
    @Getter
    @Setter
    private ClassB b;
    @Getter
    @Setter
    private UUID uid;
    @Getter
    @Setter
    private String string;
    @Getter
    @Setter
    private byte[] blob;

    public static ClassA create(int seed) {
        ClassB classB = ClassB.create(seed);
        ClassA classA = new ClassA();
        classA.b = classB;
        classA.uid = new UUID(seed, seed + 50);
        classA.string = Integer.toHexString(seed);
        classA.blob = new byte[]{(byte) seed};
        return classA;
    }
}
