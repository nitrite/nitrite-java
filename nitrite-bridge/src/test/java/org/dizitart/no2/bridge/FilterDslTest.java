package org.dizitart.no2.bridge;

import org.dizitart.dbinspect.BridgeErrorKind;
import org.dizitart.dbinspect.BridgeException;
import org.dizitart.dbinspect.QueryPage;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.dizitart.no2.bridge.NitriteAdapterTest.filtered;
import static org.dizitart.no2.bridge.NitriteAdapterTest.leaf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * The filter DSL is checked by what comes back, not by what {@link org.dizitart.no2.filters.Filter}
 * it built: a translation that produces a plausible filter matching the wrong rows is exactly the
 * failure this has to catch, and only the rows can tell.
 */
public class FilterDslTest {

    private Nitrite db;
    private NitriteAdapter adapter;

    @Before
    public void open() {
        db = Fixtures.memoryDb();
        for (Object[] row :
                new Object[][] {
                    {1, "ada", 36, 1.5},
                    {2, "grace", 45, 2.5},
                    {3, "alan", 41, 3.5},
                    {4, "edsger", 72, 4.5},
                }) {
            db.getCollection("people")
                    .insert(
                            Document.createDocument()
                                    .put("id", row[0])
                                    .put("name", row[1])
                                    .put("age", row[2])
                                    .put("rating", row[3])
                                    .put("bio", row[1] + " liked counting things"));
        }
        // One full-text index, so the text operator has both a field it can serve and a field it
        // cannot.
        db.getCollection("people").createIndex(IndexOptions.indexOptions(IndexType.FULL_TEXT), "bio");
        adapter = NitriteAdapter.builder(db, "main", "app.db").build();
    }

    @After
    public void close() {
        db.close();
    }

    @Test
    public void everyAdvertisedOperatorRoundTrips() {
        // The acceptance criterion is on filterOps, so every operator named there gets a row check
        // — an operator that is advertised and mistranslated is worse than one that is absent.
        assertEquals(
                Arrays.asList("eq", "ne", "gt", "gte", "lt", "lte", "in", "notIn", "text"),
                adapter.capabilities().filterOps());

        assertEquals(ids(1), ids(leaf("name", "eq", "ada")));
        assertEquals(ids(2, 3, 4), ids(leaf("name", "ne", "ada")));
        assertEquals(ids(2, 4), ids(leaf("age", "gt", 41L)));
        assertEquals(ids(2, 3, 4), ids(leaf("age", "gte", 41L)));
        assertEquals(ids(1), ids(leaf("age", "lt", 41L)));
        assertEquals(ids(1, 3), ids(leaf("age", "lte", 41L)));
        assertEquals(ids(1, 3), ids(leaf("name", "in", Arrays.asList("ada", "alan"))));
        assertEquals(ids(2, 4), ids(leaf("name", "notIn", Arrays.asList("ada", "alan"))));
        // "text" only where the field carries a full-text index; see below.
        assertEquals(ids(1), ids(leaf("bio", "text", "ada")));
    }

    @Test
    public void textIsRefusedByFieldRatherThanFailingInsideTheEngine() {
        // Nitrite's FindOptimizer refuses a text filter it cannot serve from a full-text index,
        // and filterOps is a flat operator list that cannot say "text, but only on bio". Without
        // this check the client would get a bare "internal error" for a perfectly legible mistake.
        refuses(leaf("name", "text", "ada"));
    }

    @Test
    public void aJsonNumberMatchesAStoredIntegerOfADifferentWidth() {
        // Gson hands the tree over as Long; the store holds Integer. Nitrite compares numerically,
        // and a bridge that relied on equals() would silently return nothing here.
        assertEquals(ids(2), ids(leaf("age", "eq", 45L)));
        assertEquals(ids(1), ids(leaf("rating", "lt", 2L)));
    }

    @Test
    public void andOrAndNotCompose() {
        assertEquals(
                ids(3),
                ids(tree("and", leaf("age", "gt", 36L), leaf("age", "lt", 45L))));
        assertEquals(
                ids(1, 4),
                ids(tree("or", leaf("name", "eq", "ada"), leaf("name", "eq", "edsger"))));
        assertEquals(ids(2, 3, 4), ids(node("not", leaf("name", "eq", "ada"))));
    }

    @Test
    public void aSingleClauseAndIsItself() {
        // PROTOCOL.md's own queryPage example sends a one-element `and`, and Nitrite's and() wants
        // at least two operands.
        assertEquals(ids(1), ids(tree("and", leaf("name", "eq", "ada"))));
    }

    @Test
    public void anOperatorThisAdapterDoesNotHaveIsRefusedRatherThanApproximated() {
        // `exists` is a v1 operator nitrite-java has no filter for. Returning an unfiltered page
        // would show the developer rows they explicitly excluded.
        refuses(leaf("name", "exists", true));
        refuses(leaf("name", "between", Arrays.asList(1L, 2L)));
    }

    @Test
    public void aMalformedNodeIsRefused() {
        refuses(leaf("", "eq", 1L));
        refuses(withoutOp());
        refuses(node("and", "age > 30"));
        refuses(node("or", Collections.singletonList("age > 30")));
        refuses(emptyList("and"));
        refuses(emptyList("or"));
    }

    @Test
    public void anOrderingComparisonNeedsSomethingTheStoreCanOrder() {
        // A boolean or an object would throw inside the engine mid-page; refusing names it.
        refuses(leaf("age", "gt", Boolean.TRUE));
        refuses(leaf("age", "gt", Collections.singletonMap("a", 1L)));
        refuses(leaf("name", "in", "ada"));
        refuses(leaf("name", "in", new ArrayList<>()));
        refuses(leaf("name", "text", 1L));
    }

    @Test
    public void aFilterNestedDeeperThanTheLimitIsRefusedRatherThanOverflowingTheStack() {
        Map<String, Object> deep = leaf("name", "eq", "ada");
        for (int i = 0; i <= FilterDsl.MAX_FILTER_DEPTH; i++) {
            deep = node("not", deep);
        }
        refuses(deep);
    }

    // --- regex, threat model F10 / criterion 9 -------------------------------------------------

    @Test
    public void regexIsRefusedUntilTheDeveloperOptsIn() {
        refuses(leaf("name", "regex", "^a"));
    }

    @Test
    public void withRegexOnAPlainPatternWorks() {
        assertEquals(ids(1, 3), ids(permissive(), leaf("name", "regex", "^a")));
    }

    @Test
    public void aNestedQuantifierIsRefusedEvenWithRegexOn() {
        // (a+)+$ against a 40-character string is criterion 9's own example. Nothing can interrupt
        // a Java match once it has started backtracking, so the refusal is the mitigation.
        for (String pattern : new String[] {"(a+)+$", "(?:a*)*b", "(a{1,3})+"}) {
            try {
                run(permissive(), leaf("name", "regex", pattern));
                fail("expected " + pattern + " to be refused");
            } catch (BridgeException refused) {
                assertEquals(BridgeErrorKind.BAD_REQUEST, refused.kind());
            }
        }
    }

    @Test
    public void anOverlongOrInvalidPatternIsRefused() {
        StringBuilder overlong = new StringBuilder();
        for (int i = 0; i <= FilterDsl.MAX_REGEX_PATTERN_LENGTH; i++) {
            overlong.append('a');
        }
        try {
            run(permissive(), leaf("name", "regex", overlong.toString()));
            fail("expected an overlong pattern to be refused");
        } catch (BridgeException refused) {
            assertEquals(BridgeErrorKind.BAD_REQUEST, refused.kind());
        }
        try {
            run(permissive(), leaf("name", "regex", "["));
            fail("expected an invalid pattern to be refused");
        } catch (BridgeException refused) {
            assertEquals(BridgeErrorKind.BAD_REQUEST, refused.kind());
        }
    }

    // --- helpers ------------------------------------------------------------------------------

    private NitriteAdapter permissive() {
        return NitriteAdapter.builder(db, "main", "app.db").allowRegex(true).build();
    }

    private List<Integer> ids(Map<String, Object> filter) {
        return ids(adapter, filter);
    }

    private List<Integer> ids(NitriteAdapter on, Map<String, Object> filter) {
        List<Integer> ids = new ArrayList<>();
        for (Map<String, Object> row : run(on, filter).rows()) {
            ids.add(((Number) row.get("id")).intValue());
        }
        Collections.sort(ids);
        return ids;
    }

    private QueryPage run(NitriteAdapter on, Map<String, Object> filter) {
        return on.queryPage(filtered("people", filter));
    }

    private static List<Integer> ids(Integer... expected) {
        return Arrays.asList(expected);
    }

    private void refuses(Map<String, Object> filter) {
        try {
            run(adapter, filter);
            fail("expected " + filter + " to be refused");
        } catch (BridgeException refused) {
            assertEquals(BridgeErrorKind.BAD_REQUEST, refused.kind());
        }
    }

    @SafeVarargs
    private static Map<String, Object> tree(String combinator, Map<String, Object>... children) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put(combinator, new ArrayList<>(Arrays.asList(children)));
        return node;
    }

    private static Map<String, Object> node(String key, Object value) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put(key, value);
        return node;
    }

    private static Map<String, Object> emptyList(String key) {
        return node(key, new ArrayList<>());
    }

    private static Map<String, Object> withoutOp() {
        return node("field", "name");
    }
}
