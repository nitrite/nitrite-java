package org.dizitart.no2.common.tuples;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Represents a quintet.
 *
 * @param <A> the type parameter
 * @param <B> the type parameter
 * @param <C> the type parameter
 * @param <D> the type parameter
 * @param <E> the type parameter
 * @author Anindya Chatterjee
 * @since 4.0
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

    /**
     * Creates a new quintet.
     *
     * @param <A> the type parameter
     * @param <B> the type parameter
     * @param <C> the type parameter
     * @param <D> the type parameter
     * @param <E> the type parameter
     * @param a   the a
     * @param b   the b
     * @param c   the c
     * @param d   the d
     * @param e   the e
     * @return the quintet
     */
    public static <A, B, C, D, E> Quintet<A, B, C, D, E> quintet(A a, B b, C c, D d, E e) {
        return new Quintet<>(a, b, c, d, e);
    }

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
