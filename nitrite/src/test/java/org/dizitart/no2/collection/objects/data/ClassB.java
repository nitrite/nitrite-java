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

@EqualsAndHashCode
@ToString
class ClassB implements Comparable<ClassB> {
    @Getter @Setter private int number;
    @Getter @Setter private String text;

    static ClassB create(int seed) {
        ClassB classB = new ClassB();
        classB.setNumber(seed + 100);
        classB.setText(Integer.toBinaryString(seed));
        return classB;
    }

    @Override
    public int compareTo(ClassB o) {
        return Integer.compare(number, o.number);
    }
}
