package org.dizitart.no2.bridge;

import org.dizitart.dbinspect.BridgeErrorKind;
import org.dizitart.dbinspect.BridgeException;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.filters.FluentFilter;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.IndexType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Turns the wire filter tree of {@code docs/PROTOCOL.md} §4.1 into a Nitrite {@link Filter}.
 *
 * <p>Every refusal is a {@link BridgeException} with {@code badRequest}, never a silently dropped
 * clause: returning an unfiltered page would show the developer rows they explicitly excluded.
 */
final class FilterDsl {

    /**
     * The v1 filter operators this implementation actually supports.
     *
     * <p><b>{@code exists} was absent until nitrite-java 5.0.0 and is now here.</b> It is listed
     * among the v1 operators in {@code docs/PROTOCOL.md} §4.1 and was reported unsupported for as
     * long as {@link FluentFilter} had nothing that tested for a field's presence; 4.5.0 added
     * {@code where(f).exists()} and this adapter maps onto it. {@code capabilities.filterOps} stays
     * authoritative either way — that is the mechanism that let the gap be honest instead of
     * mistranslated, and it is what makes closing it a one-line change.
     *
     * <p>{@code between} and {@code elemMatch} are the other direction — Nitrite has them and v1
     * does not, so they stay out until the protocol gains them.
     */
    static final List<String> FILTER_OPS =
            Collections.unmodifiableList(
                    Arrays.asList(
                            "eq", "ne", "gt", "gte", "lt", "lte", "in", "notIn", "exists", "text"));

    /** Reported in {@code filterOps} only when the developer set {@code allowRegex} (F10). */
    static final String REGEX_OP = "regex";

    /** Threat model F10 fix 3, and explicitly best-effort. */
    static final int MAX_REGEX_PATTERN_LENGTH = 256;

    /**
     * A filter tree arrives from a paired but untrusted client, and each level is a stack frame.
     * Deep enough nesting is a crash inside the developer's application, which is the same class of
     * problem as an unbounded frame.
     */
    static final int MAX_FILTER_DEPTH = 16;

    /**
     * A group that ends in a quantifier and is itself quantified — {@code (a+)+}, {@code (?:a*)*},
     * {@code (a{1,3})+}. This is the shape behind exponential backtracking, and it is the pattern
     * criterion 9 names.
     */
    private static final Pattern NESTED_QUANTIFIER = Pattern.compile("[*+}]\\)[*+{]");

    private FilterDsl() {}

    /**
     * @param collection the store being queried, needed only by {@code text} — see
     *     {@link #text(Object, String, NitriteCollection)}
     */
    static Filter parse(Map<String, Object> tree, boolean allowRegex, NitriteCollection collection) {
        return parse(tree, allowRegex, collection, 0);
    }

    private static Filter parse(
            Object node, boolean allowRegex, NitriteCollection collection, int depth) {
        if (depth > MAX_FILTER_DEPTH) {
            throw new BridgeException(BridgeErrorKind.BAD_REQUEST, "filter is nested too deeply");
        }
        if (!(node instanceof Map)) {
            throw new BridgeException(BridgeErrorKind.BAD_REQUEST, "a filter node must be an object");
        }

        Map<?, ?> object = (Map<?, ?>) node;
        Object conjunction = object.get("and");
        if (conjunction != null) {
            return combine(conjunction, allowRegex, collection, depth, true);
        }
        Object disjunction = object.get("or");
        if (disjunction != null) {
            return combine(disjunction, allowRegex, collection, depth, false);
        }
        Object negation = object.get("not");
        if (negation != null) {
            return parse(negation, allowRegex, collection, depth + 1).not();
        }
        return leaf(object, allowRegex, collection);
    }

    private static Filter combine(
            Object raw,
            boolean allowRegex,
            NitriteCollection collection,
            int depth,
            boolean conjunction) {
        if (!(raw instanceof List) || ((List<?>) raw).isEmpty()) {
            throw new BridgeException(
                    BridgeErrorKind.BAD_REQUEST, "and/or takes a non-empty list of filters");
        }
        List<Filter> parts = new ArrayList<>();
        for (Object child : (List<?>) raw) {
            parts.add(parse(child, allowRegex, collection, depth + 1));
        }
        // Nitrite's and/or want at least two operands, and the protocol's own queryPage example
        // sends a one-element `and`. One clause is itself.
        if (parts.size() == 1) {
            return parts.get(0);
        }
        Filter[] operands = parts.toArray(new Filter[0]);
        return conjunction ? Filter.and(operands) : Filter.or(operands);
    }

    private static Filter leaf(Map<?, ?> node, boolean allowRegex, NitriteCollection collection) {
        Object field = node.get("field");
        Object op = node.get("op");
        if (!(field instanceof String) || ((String) field).isEmpty()) {
            throw new BridgeException(BridgeErrorKind.BAD_REQUEST, "a filter needs a field name");
        }
        if (!(op instanceof String)) {
            throw new BridgeException(BridgeErrorKind.BAD_REQUEST, "a filter needs an operator");
        }

        Object value = node.get("value");
        FluentFilter on = FluentFilter.where((String) field);
        String operator = (String) op;
        switch (operator) {
            case "eq":
                return on.eq(value);
            case "ne":
                return on.notEq(value);
            case "gt":
                return on.gt(ordered(value, operator));
            case "gte":
                return on.gte(ordered(value, operator));
            case "lt":
                return on.lt(ordered(value, operator));
            case "lte":
                return on.lte(ordered(value, operator));
            case "in":
                return on.in(orderedList(value, operator));
            case "notIn":
                return on.notIn(orderedList(value, operator));
            case "exists":
                // Presence only, and `value` is deliberately ignored: `exists: false` is not "does
                // not exist" in the protocol — that is `not`. Reading the value would select the
                // opposite rows for a client that sent one out of habit.
                return on.exists();
            case "text":
                return on.text(text(value, (String) field, collection));
            case REGEX_OP:
                if (!allowRegex) {
                    // F10's load-bearing mitigation: off unless the developer opted in, and absent
                    // from filterOps when off.
                    throw new BridgeException(
                            BridgeErrorKind.BAD_REQUEST, "regex is not enabled on this adapter");
                }
                return on.regex(pattern(value));
            default:
                throw new BridgeException(
                        BridgeErrorKind.BAD_REQUEST,
                        "this adapter does not support the \"" + operator + "\" operator");
        }
    }

    /**
     * Ordering comparisons need something the store can order. A boolean or an object would throw
     * inside the engine mid-page; refusing here names the problem.
     */
    private static Comparable<?> ordered(Object value, String op) {
        if (value instanceof Number || value instanceof String) {
            return (Comparable<?>) value;
        }
        throw new BridgeException(
                BridgeErrorKind.BAD_REQUEST,
                "\"" + op + "\" needs a number or a string to compare against");
    }

    private static Comparable<?>[] orderedList(Object value, String op) {
        if (!(value instanceof List) || ((List<?>) value).isEmpty()) {
            throw new BridgeException(
                    BridgeErrorKind.BAD_REQUEST, "\"" + op + "\" needs a non-empty list of values");
        }
        List<?> raw = (List<?>) value;
        Comparable<?>[] values = new Comparable<?>[raw.size()];
        for (int i = 0; i < raw.size(); i++) {
            values[i] = ordered(raw.get(i), op);
        }
        return values;
    }

    /**
     * <b>{@code text} needs a full-text index on the field, and that is not expressible in
     * {@code filterOps}</b>, which is a flat list of operators rather than a per-column matrix.
     * Nitrite's own {@code FindOptimizer} refuses a text filter it cannot serve from an index, so
     * without this check the developer would see a bare "internal error" on the grid and the real
     * reason only in their application's log. Named here, the message is also the fix.
     */
    private static String text(Object value, String field, NitriteCollection collection) {
        if (!(value instanceof String) || ((String) value).isEmpty()) {
            throw new BridgeException(
                    BridgeErrorKind.BAD_REQUEST, "\"text\" needs a string to search for");
        }
        if (!isFullTextIndexed(collection, field)) {
            throw new BridgeException(
                    BridgeErrorKind.BAD_REQUEST,
                    "\"" + field + "\" has no full-text index, so it cannot be text-searched");
        }
        return (String) value;
    }

    private static boolean isFullTextIndexed(NitriteCollection collection, String field) {
        for (IndexDescriptor index : collection.listIndices()) {
            if (IndexType.FULL_TEXT.equals(index.getIndexType())
                    && index.getFields().getFieldNames().contains(field)) {
                return true;
            }
        }
        return false;
    }

    /**
     * F10 fix 3: a length cap and a best-effort nested-quantifier refusal.
     *
     * <p><b>Best-effort is the honest word.</b> {@link java.util.regex.Matcher} does not check for
     * interruption, and the session's deadline deliberately bounds the answer rather than the work,
     * so nothing can rescue a pattern that has already started backtracking — which is why
     * {@code allowRegex} defaulting to off is the mitigation that carries the weight, and this one
     * is a second line rather than the line.
     */
    private static String pattern(Object value) {
        if (!(value instanceof String) || ((String) value).isEmpty()) {
            throw new BridgeException(BridgeErrorKind.BAD_REQUEST, "\"regex\" needs a pattern");
        }
        String candidate = (String) value;
        if (candidate.length() > MAX_REGEX_PATTERN_LENGTH) {
            throw new BridgeException(
                    BridgeErrorKind.BAD_REQUEST,
                    "regex patterns are limited to " + MAX_REGEX_PATTERN_LENGTH + " characters");
        }
        if (NESTED_QUANTIFIER.matcher(candidate).find()) {
            throw new BridgeException(
                    BridgeErrorKind.BAD_REQUEST,
                    "this regex has a nested quantifier and is refused: it can take exponential "
                            + "time, and a Java match cannot be interrupted once it has started");
        }
        try {
            Pattern.compile(candidate);
        } catch (PatternSyntaxException invalid) {
            throw new BridgeException(
                    BridgeErrorKind.BAD_REQUEST, "this regex is not a valid pattern");
        }
        return candidate;
    }
}
