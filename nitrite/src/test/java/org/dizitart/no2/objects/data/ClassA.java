package org.dizitart.no2.objects.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@EqualsAndHashCode
@ToString
public class ClassA {
    @Getter @Setter private ClassB classB;
    @Getter @Setter private UUID uid;
    @Getter @Setter private String string;
    @Getter @Setter private byte[] blob;

    public static ClassA create(int seed) {
        ClassB classB = ClassB.create(seed);
        ClassA classA = new ClassA();
        classA.classB = classB;
        classA.uid = new UUID(seed, seed + 50);
        classA.string = Integer.toHexString(seed);
        classA.blob = new byte[] {(byte) seed};
        return classA;
    }
}
