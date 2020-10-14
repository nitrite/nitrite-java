package org.dizitart.no2.filters;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.operation.FindOptions;
import org.dizitart.no2.common.FieldValues;
import org.dizitart.no2.common.RecordStream;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.index.BaseNitriteIndexer;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.store.NitriteMap;

/**
 * @author Anindya Chatterjee
 */
public class QueryOptimizer {
    private BaseNitriteIndexer nitriteIndexer;
    private NitriteConfig nitriteConfig;

    public RecordStream<Pair<NitriteId, Document>> findOptimizedStream(Filter filter,
                                                                       FindOptions findOptions) {
        if (filter == null) {
            return findOptimizedStreamByOption(findOptions);
        }

        // 1. AND
        // 2. OR (AND)
        // 3. Union stream (for OR call recursively this method)

        if (filter instanceof AndFilter) {
            AndFilter andFilter = (AndFilter) filter;
            FieldValues fieldValues = decompose(andFilter);
            IndexDescriptor descriptor = findSuitableIndex(fieldValues);
            
            if (nitriteIndexer.isCompoundIndex(descriptor)) {
                NitriteMap<?, ?> indexMap = nitriteIndexer.getCompoundIndexMap(descriptor,
                    nitriteConfig.getNitriteStore(), fieldValues.getFirstValue().getClass());
            }
        }
    }

    private RecordStream<Pair<NitriteId, Document>> findOptimizedStreamByOption(FindOptions findOptions) {
        return null;
    }

    private FieldValues decompose(AndFilter filter) {
        FieldValues fieldValues = new FieldValues();

        Filter lhs = filter.getLhs();
        Filter rhs = filter.getRhs();

        if (lhs instanceof ComparisonFilter) {
            
        }
    }

    private IndexDescriptor findSuitableIndex(FieldValues fieldValues) {
        return null;
    }
}
