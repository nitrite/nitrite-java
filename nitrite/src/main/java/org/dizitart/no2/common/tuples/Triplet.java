package org.dizitart.no2.common.tuples;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**
 * A generic class representing a tuple of three elements.
 *
 * @param <A> the type of the first element
 * @param <B> the type of the second element
 * @param <C> the type of the third element
 * @author Anindya Chatterjee.
 * @since 4.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Triplet<A, B, C> implements Serializable {
    private static final long serialVersionUID = 1599480644L;

    private A first;
    private B second;
    private C third;

    /**
     * Creates a new triplet.
     *
     * @param <A> the type parameter
     * @param <B> the type parameter
     * @param <C> the type parameter
     * @param a   the a
     * @param b   the b
     * @param c   the c
     * @return the triplet
     */
    public static <A, B, C> Triplet<A, B, C> triplet(A a, B b, C c) {
        return new Triplet<>(a, b, c);
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(first);
        stream.writeObject(second);
        stream.writeObject(third);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        first = (A) stream.readObject();
        second = (B) stream.readObject();
        third = (C) stream.readObject();
    }
}
