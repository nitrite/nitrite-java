package org.dizitart.no2.objects.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class ClassC {
    @Getter @Setter private long id;
    @Getter @Setter private double digit;
    @Getter @Setter private ClassA parent;

    public static ClassC create(int seed) {
        ClassC classC = new ClassC();
        classC.id = seed * 5000;
        classC.digit = seed * 69.65;
        classC.parent = ClassA.create(seed);
        return classC;
    }
}
