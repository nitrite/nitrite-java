package org.dizitart.no2.common.tuples;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * A tuple of four elements.
 *
 * @param <A> the type of the first element
 * @param <B> the type of the second element
 * @param <C> the type of the third element
 * @param <D> the type of the fourth element
 * @author Anindya Chatterjee
 * @since 4.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quartet<A, B, C, D> implements Serializable {
    private static final long serialVersionUID = 1599826800L;

    private A first;
    private B second;
    private C third;
    private D fourth;

    /**
     * Creates a new quartet.
     *
     * @param <A> the type parameter
     * @param <B> the type parameter
     * @param <C> the type parameter
     * @param <D> the type parameter
     * @param a   the a
     * @param b   the b
     * @param c   the c
     * @param d   the d
     * @return the quartet
     */
    public static <A, B, C, D> Quartet<A, B, C, D> quartet(A a, B b, C c, D d) {
        return new Quartet<>(a, b, c, d);
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(first);
        stream.writeObject(second);
        stream.writeObject(third);
        stream.writeObject(fourth);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        first = (A) stream.readObject();
        second = (B) stream.readObject();
        third = (C) stream.readObject();
        fourth = (D) stream.readObject();
    }
}
