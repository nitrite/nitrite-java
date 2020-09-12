package org.dizitart.no2.common.tuples;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author Anindya Chatterjee
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quintet<A, B, C, D, E> implements Serializable {
    private static final long serialVersionUID = 1599827018L;

    private A first;
    private B second;
    private C third;
    private D fourth;
    private E fifth;

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(first);
        stream.writeObject(second);
        stream.writeObject(third);
        stream.writeObject(fourth);
        stream.writeObject(fifth);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        first = (A) stream.readObject();
        second = (B) stream.readObject();
        third = (C) stream.readObject();
        fourth = (D) stream.readObject();
        fifth = (E) stream.readObject();
    }
}
