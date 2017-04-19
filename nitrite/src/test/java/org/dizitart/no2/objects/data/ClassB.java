package org.dizitart.no2.objects.data;

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
