package org.dizitart.no2.filters;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.operation.FindOptions;
import org.dizitart.no2.common.*;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.store.IndexCatalog;
import org.dizitart.no2.store.NitriteMap;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;


/**
 * @author Anindya Chatterjee
 */
public class QueryOptimizer {
    private final NitriteConfig nitriteConfig;

    /*
     * 1. If And filter
     *   1.1 flatten the filter using depth first traversal
     *   1.2 check each filter, group OR & AND, single field filters
     *   1.3 scan through single field filter or and filter if there is any match for composite index
     *       1.3.1 if matching composite index found, get index stream
     *       1.3.2 group remaining filters as a new and filter and apply on indexed stream from 1.3.1
     *   1.4 if no matching composite index found, scan for simple index
     *       1.4.1 if found, get indexed stream
     *       1.4.2 group remaining filter as a new and filter and apply on index stream from 1.4.1
     *   1.5 if no matching index found, collscan and apply filter
     *
     * 2. If OR filter
     *   1.1 If every simple field is indexed or and filter composite indexed
     *       1.1.1
     *   1.2 If one of the fields is not indexed, get collscan and apply filter
     *
     * 3. If simple filter
     *   3.1 Check if index exists, send indexed stream
     *   3.2 If no index found, collscan
     *
     * */

    public QueryOptimizer(NitriteConfig nitriteConfig) {
        this.nitriteConfig = nitriteConfig;
    }

    public FilterStep optimizeFilter(NitriteMap<NitriteId, Document> primaryCollection,
                                     Filter filter,
                                     FindOptions findOptions) {
        List<Filter> flattenedFilters = new ArrayList<>();
        if (filter instanceof AndFilter) {
            List<Filter> filters = flattenAndFilter((AndFilter) filter);
            flattenedFilters.addAll(filters);
        } else {
            flattenedFilters.add(filter);
        }

        return null;
    }

    private List<Filter> flattenAndFilter(AndFilter andFilter) {
        List<Filter> flattenedFilters = new ArrayList<>();
        if (andFilter != null) {
            for (Filter filter : andFilter.getFilters()) {
                if (filter instanceof AndFilter) {
                    List<Filter> filters = flattenAndFilter((AndFilter) filter);
                    flattenedFilters.addAll(filters);
                } else {
                    flattenedFilters.add(filter);
                }
            }
        }
        return flattenedFilters;
    }

    private Pair<Filter, Filter> optimizeAnd(String collectionName, List<Filter> flattenedFilter) {
        IndexCatalog indexCatalog = nitriteConfig.getNitriteStore().getIndexCatalog();
        Set<IndexedFieldNames> indexedFieldNames = indexCatalog.findIndexSupportedFields(collectionName);
        Set<FilterFieldNames> filterFieldNames = getFieldNames(flattenedFilter);


        return null;
    }

    private Set<FilterFieldNames> getFieldNames(List<Filter> filters) {
        // send only eligible field names, ignore OR filers as they can't be used for composite index
        return null;
    }





















    @SuppressWarnings("unchecked")
    public RecordStream<Pair<NitriteId, Document>> findOptimizedStream(NitriteMap<NitriteId, Document> primaryCollection,
                                                                       Filter filter,
                                                                       FindOptions findOptions) {
        if (filter == null) {
            return optimizedStream(null, findOptions, primaryCollection);
        }

        if (filter instanceof AndFilter) {
            AndFilter andFilter = (AndFilter) filter;
            FieldValues fieldValues = decomposeAnd(andFilter);
            NitriteMap<Comparable<?>, ?> indexMap = findSuitableIndexMap(primaryCollection.getName(), fieldValues);
            return optimizedStream(filter, findOptions, primaryCollection, indexMap);
        } else if (filter instanceof OrFilter) {
            OrFilter orFilter = (OrFilter) filter;
            List<FieldValues> fieldValuesList = decomposeOr(orFilter);
            List<NitriteMap<?, ?>> indexedMaps = new ArrayList<>();

            for (FieldValues fieldValues : fieldValuesList) {
                NitriteMap<Comparable<?>, ?> indexMap = findSuitableIndexMap(primaryCollection.getName(), fieldValues);
                if (indexMap != null) {
                    indexedMaps.add(indexMap);
                }
            }

            return optimizedStream(filter, findOptions, primaryCollection, indexedMaps.toArray(new NitriteMap[0]));
        } else if (filter instanceof IndexAwareFilter) {
            FieldValues fieldValues = decomposeFilter(filter);
            NitriteMap<Comparable<?>, ?> indexMap = findSuitableIndexMap(primaryCollection.getName(), fieldValues);
            return optimizedStream(filter, findOptions, primaryCollection, indexMap);
        } else {
            return optimizedStream(filter, findOptions, primaryCollection);
        }
    }

    private FieldValues decomposeFilter(Filter filter) {
        FieldValues fieldValues = new FieldValues();
        if (filter instanceof ComparisonFilter) {
            ComparisonFilter comparisonFilter = (ComparisonFilter) filter;
            fieldValues.getValues().add(new Pair<>(comparisonFilter.getField(), comparisonFilter.getComparable()));
        } else if (filter instanceof AndFilter) {
            AndFilter andFilter = (AndFilter) filter;
            FieldValues fv = decomposeAnd(andFilter);
            fieldValues.getValues().addAll(fv.getValues());
        }
        return fieldValues;
    }

    private FieldValues decomposeAnd(AndFilter filter) {
        FieldValues fieldValues = new FieldValues();

        Filter lhs = filter.getLhs();
        Filter rhs = filter.getRhs();

        // if there are any OR filter in any of the arms, nitrite will not consider it
        FieldValues lhsFieldValues = decomposeFilter(lhs);
        FieldValues rhsFieldValues = decomposeFilter(rhs);
        fieldValues.getValues().addAll(lhsFieldValues.getValues());
        fieldValues.getValues().addAll(rhsFieldValues.getValues());
        return fieldValues;
    }

    private List<FieldValues> decomposeOr(OrFilter filter) {
        List<FieldValues> fieldValues = new ArrayList<>();

        Filter lhs = filter.getLhs();
        Filter rhs = filter.getRhs();

        if (lhs instanceof OrFilter) {
            fieldValues.addAll(decomposeOr((OrFilter) lhs));
        } else {
            fieldValues.add(decomposeFilter(lhs));
        }

        if (rhs instanceof OrFilter) {
            fieldValues.addAll(decomposeOr((OrFilter) rhs));
        } else {
            fieldValues.add(decomposeFilter(rhs));
        }

        return fieldValues;
    }

    private NitriteMap<Comparable<?>, ?> findSuitableIndexMap(String collectionName, FieldValues fieldValues) {
        IndexCatalog indexCatalog = nitriteConfig.getNitriteStore().getIndexCatalog();
        IndexDescriptor descriptor = null;

        if (indexCatalog != null) {
            NavigableMap<Integer, IndexDescriptor> indexScore = new TreeMap<>(Collections.reverseOrder());

            Fields fields = fieldValues.getFields();
            Collection<IndexDescriptor> indexDescriptors = indexCatalog.listIndexDescriptors(collectionName);
            for (IndexDescriptor indexDescriptor : indexDescriptors) {
                Fields indexFields = indexDescriptor.getIndexFields();
                if (indexFields.isPrefix(fields)) {
                    int keySize = indexFields.getFieldNames().size();
                    int score = IndexType.Unique.equals(indexDescriptor.getIndexType()) ? keySize + 10 : keySize;
                    indexScore.put(score, indexDescriptor);
                }
            }

            if (!indexScore.isEmpty()) {
                descriptor = indexScore.firstEntry().getValue();
            }
        }

        return getIndexMap(descriptor, fieldValues);
    }

    private NitriteMap<Comparable<?>, ?> getIndexMap(IndexDescriptor indexDescriptor, FieldValues fieldValues) {
        if (indexDescriptor == null) return null;

        String indexMapName = nitriteConfig.getNitriteStore().getIndexCatalog().getIndexMapName(indexDescriptor);
        Class<?> keyType = fieldValues.getFirstValue() == null ? Comparable.class
            : fieldValues.getFirstValue().getClass();
        Class<?> valueType = indexDescriptor.isCompoundIndex() ? ConcurrentSkipListMap.class
            : ConcurrentSkipListSet.class;

        return nitriteConfig.getNitriteStore().openMap(indexMapName, keyType, valueType);
    }


    @SafeVarargs
    private final RecordStream<Pair<NitriteId, Document>> optimizedStream(Filter filter,
                                                                          FindOptions findOptions,
                                                                          NitriteMap<NitriteId, Document> primaryCollection,
                                                                          NitriteMap<Comparable<?>, ?>... indexMaps) {
        if (filter == null) {
            return primaryCollection.entries();
        }

        
    }
}
