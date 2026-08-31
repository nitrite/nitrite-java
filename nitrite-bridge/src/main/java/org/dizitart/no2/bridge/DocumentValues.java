package org.dizitart.no2.bridge;

import org.dizitart.dbinspect.Values;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.tuples.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Nitrite's values as the wire carries them.
 *
 * <p>{@link Values} in the core is engine-neutral and so knows nothing of {@link Document}, which
 * is neither a {@code Map} nor a {@code Collection} and would degrade to its {@code toString}. A
 * repository row with a nested entity in it is the common case rather than an exotic one, so this
 * unwraps Nitrite's own type and hands the rest to the core.
 *
 * <p><b>Public because there is a second caller, and a second encoder is the thing that drifts.</b>
 * Fanlight's {@code sidecar-jvm} reads a database from a path rather than from a running
 * application, so it cannot use {@link NitriteAdapter} — that one opens an {@code ObjectRepository}
 * by Java type, and a sidecar handed a path has no types to hand in. It drives {@code NitriteStore}
 * directly and encodes the documents it finds with these two methods. The Rust side settled the
 * same question the same way: {@code nitrite-bridge}'s {@code values} module is what {@code
 * sidecar-rust} imports rather than copying.
 */
public final class DocumentValues {

    private DocumentValues() {}

    /** One value, JSON-safe, with {@link Document} and {@link Iterable} unwrapped recursively. */
    public static Object encode(Object value) {
        if (value instanceof Document) {
            Map<String, Object> nested = new LinkedHashMap<>();
            for (Pair<String, Object> field : (Document) value) {
                nested.put(field.getFirst(), encode(field.getSecond()));
            }
            return nested;
        }
        if (value instanceof Iterable) {
            List<Object> encoded = new ArrayList<>();
            for (Object element : (Iterable<?>) value) {
                encoded.add(encode(element));
            }
            return encoded;
        }
        return Values.encode(value);
    }

    /**
     * A closed set, so the client has a known set of renderings rather than whatever {@code
     * getClass().getSimpleName()} prints. Null means this document said nothing about the field's
     * type.
     */
    public static String typeOf(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return "bool";
        }
        if (value instanceof Integer
                || value instanceof Long
                || value instanceof Short
                || value instanceof Byte) {
            return "int";
        }
        if (value instanceof Number) {
            return "real";
        }
        if (value instanceof CharSequence) {
            return "text";
        }
        if (value instanceof Date || value instanceof java.time.temporal.Temporal) {
            return "date";
        }
        if (value instanceof byte[]) {
            return "blob";
        }
        // Before the Iterable branch on purpose: Document is itself an Iterable of its fields, and
        // a nested document reported as a list is a wrong rendering rather than a vague one.
        if (value instanceof Document || value instanceof Map) {
            return "document";
        }
        if (value instanceof Iterable || value.getClass().isArray()) {
            return "list";
        }
        return "text";
    }
}
