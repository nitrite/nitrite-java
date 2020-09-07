package org.dizitart.no2.common.tuples;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Represents a triplet.
 *
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
