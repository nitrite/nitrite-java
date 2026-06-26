/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.index;

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.DBNull;
import org.dizitart.no2.common.DBValue;
import org.dizitart.no2.common.util.Comparables;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.filters.ComparableFilter;
import org.dizitart.no2.filters.SortingAwareFilter;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

/**
 * @author Anindya Chatterjee
 * @since 4.0
 */
public class IndexScanner {
    private final IndexMap indexMap;

    public IndexScanner(IndexMap indexMap) {
        this.indexMap = indexMap;
    }

    @SuppressWarnings("unchecked")
    public LinkedHashSet<NitriteId> doScan(List<ComparableFilter> filters, Map<String, Boolean> indexScanOrder) {
        // linked-hash-set to return only unique ids preserving the order in index
        LinkedHashSet<NitriteId> nitriteIds = new LinkedHashSet<>();

        // Multi-bound range query on a single field (e.g. `age >= 30 AND age <= 50`):
        // evaluate the bounds against the index and return only the ids whose key falls inside
        // the range - instead of every id above the lower bound, post-filtered downstream.
        if (filters != null && filters.size() > 1 && allSameField(filters)) {
            // Best case: a lower + upper bound form a contiguous range - scan only the keys
            // inside it (reads in-range keys, not the whole index).
            LinkedHashSet<NitriteId> bounded = scanBoundedRange(filters, indexScanOrder);
            if (bounded != null) {
                return bounded;
            }
            // Otherwise still correct, just reads more: evaluate each bound and intersect.
            LinkedHashSet<NitriteId> intersected = scanIntersectSameField(filters, indexScanOrder);
            if (intersected != null) {
                return intersected;
            }
            // else fall through to the default (cascading) scan
        }

        if (filters != null && !filters.isEmpty()) {
            // get the first filter to start scanning
            ComparableFilter comparableFilter = filters.get(0);

            if (comparableFilter != null) {
                // set the scan order of the index map
                boolean reverseScan = (indexScanOrder != null
                    && indexScanOrder.containsKey(comparableFilter.getField()))
                    ? indexScanOrder.get(comparableFilter.getField())
                    : false;
                indexMap.setReverseScan(reverseScan);

                if (comparableFilter instanceof SortingAwareFilter) {
                    // if the filter is sorting aware, set the scan order
                    ((SortingAwareFilter) comparableFilter).setReverseScan(reverseScan);
                }

                // apply the filter on the index map
                // result can be list of nitrite ids or list of navigable maps
                List<?> scanResult = comparableFilter.applyOnIndex(indexMap);
                if (isEmptyList(scanResult)) {
                    // if list is empty then no need for further scanning
                    return nitriteIds;
                } else if (isNitriteIdList(scanResult)) {
                    // if this is a list of nitrite ids then add those to the
                    // result and no further scanning is required as we have
                    // reached the terminal nitrite ids
                    List<NitriteId> idList = (List<NitriteId>) scanResult;
                    nitriteIds.addAll(idList);
                } else if (isNavigableMapList(scanResult)) {
                    // if this is a list of sub maps, then take each of the sub map
                    // and the next filter and scan the sub map
                    List<NavigableMap<DBValue, ?>> subMaps = (List<NavigableMap<DBValue, ?>>) scanResult;
                    List<ComparableFilter> remainingFilter = filters.subList(1, filters.size());

                    for (NavigableMap<DBValue, ?> subMap : subMaps) {
                        // create an index map from the sub map and scan to get the
                        // terminal nitrite ids
                        IndexMap indexMap = new IndexMap(subMap);
                        IndexScanner subMapScanner = new IndexScanner(indexMap);
                        LinkedHashSet<NitriteId> subResult = subMapScanner.doScan(remainingFilter, indexScanOrder);
                        nitriteIds.addAll(subResult);
                    }
                }
            } else {
                // filter is not comparable filter, so index scanning can not continue
                throw new FilterException("Index scan is not supported for non comparable filter");
            }
        } else {
            // if no more filter left, get all terminal nitrite ids from
            // index map and return them in the order.
            List<NitriteId> terminalResult = indexMap.getTerminalNitriteIds();
            nitriteIds.addAll(terminalResult);
        }

        return nitriteIds;
    }

    private boolean allSameField(List<ComparableFilter> filters) {
        String first = filters.get(0).getField();
        if (first == null) {
            return false;
        }
        for (ComparableFilter filter : filters) {
            if (!first.equals(filter.getField())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Combines a single lower bound ({@code >}/{@code >=}) and a single upper bound
     * ({@code <}/{@code <=}) on the same field into one bounded scan that visits only the
     * index keys inside the range.
     * <p>
     * Returns {@code null} (so the caller falls back to intersection) when the filters are not
     * a clean lower+upper pair of comparison filters, or when a sub-map is encountered (a
     * compound-index level rather than a single-field scan).
     */
    @SuppressWarnings("unchecked")
    private LinkedHashSet<NitriteId> scanBoundedRange(List<ComparableFilter> filters,
                                                      Map<String, Boolean> indexScanOrder) {
        DBValue lowerVal = null, upperVal = null;
        boolean lowerInclusive = false, upperInclusive = false;
        boolean hasLower = false, hasUpper = false;

        for (ComparableFilter filter : filters) {
            if (!(filter instanceof SortingAwareFilter)) {
                return null;
            }
            Object value = filter.getValue();
            if (value == null || !(value instanceof Comparable)) {
                return null;
            }
            DBValue dbValue = new DBValue((Comparable<?>) value);
            switch (((SortingAwareFilter) filter).getComparisonMode()) {
                case GreaterEqual:
                    if (hasLower) return null;
                    lowerVal = dbValue; lowerInclusive = true; hasLower = true; break;
                case Greater:
                    if (hasLower) return null;
                    lowerVal = dbValue; lowerInclusive = false; hasLower = true; break;
                case LesserEqual:
                    if (hasUpper) return null;
                    upperVal = dbValue; upperInclusive = true; hasUpper = true; break;
                case Lesser:
                    if (hasUpper) return null;
                    upperVal = dbValue; upperInclusive = false; hasUpper = true; break;
                default:
                    return null;
            }
        }

        // A bounded scan needs both ends; a one-sided range is already efficient on its own.
        if (!hasLower || !hasUpper) {
            return null;
        }

        // Honour the index scan order so an explicitly sorted range query keeps using the index.
        String field = filters.get(0).getField();
        boolean reverseScan = indexScanOrder != null
            && Boolean.TRUE.equals(indexScanOrder.get(field));

        LinkedHashSet<NitriteId> nitriteIds = new LinkedHashSet<>();
        if (!reverseScan) {
            DBValue key = lowerInclusive ? indexMap.ceilingKey(lowerVal) : indexMap.higherKey(lowerVal);
            while (key != DBNull.getInstance() && key != null) {
                int cmp = Comparables.compare(key, upperVal);
                boolean withinUpper = upperInclusive ? cmp <= 0 : cmp < 0;
                if (!withinUpper) {
                    break;
                }
                Object value = indexMap.get(key);
                if (value instanceof NavigableMap) {
                    // a sub-map means this is a compound-index level, not a single-field scan
                    return null;
                } else if (value instanceof List) {
                    nitriteIds.addAll((List<NitriteId>) value);
                }
                key = indexMap.higherKey(key);
            }
        } else {
            DBValue key = upperInclusive ? indexMap.floorKey(upperVal) : indexMap.lowerKey(upperVal);
            while (key != DBNull.getInstance() && key != null) {
                int cmp = Comparables.compare(key, lowerVal);
                boolean withinLower = lowerInclusive ? cmp >= 0 : cmp > 0;
                if (!withinLower) {
                    break;
                }
                Object value = indexMap.get(key);
                if (value instanceof NavigableMap) {
                    return null;
                } else if (value instanceof List) {
                    nitriteIds.addAll((List<NitriteId>) value);
                }
                key = indexMap.lowerKey(key);
            }
        }
        return nitriteIds;
    }

    /**
     * Evaluates several filters that all target the same single field and intersects their id
     * sets (used for multi-bound range queries on a single-field index).
     * <p>
     * Returns {@code null} - so the caller falls back to the default scan - if any filter yields
     * sub-maps instead of terminal ids, which means this is a compound-index level rather than a
     * single-field scan.
     */
    @SuppressWarnings("unchecked")
    private LinkedHashSet<NitriteId> scanIntersectSameField(List<ComparableFilter> filters,
                                                            Map<String, Boolean> indexScanOrder) {
        LinkedHashSet<NitriteId> acc = null;

        for (ComparableFilter filter : filters) {
            boolean reverseScan = indexScanOrder != null
                && Boolean.TRUE.equals(indexScanOrder.get(filter.getField()));
            indexMap.setReverseScan(reverseScan);
            if (filter instanceof SortingAwareFilter) {
                ((SortingAwareFilter) filter).setReverseScan(reverseScan);
            }

            List<?> result = filter.applyOnIndex(indexMap);
            if (result == null || result.isEmpty()) {
                // nothing can survive intersection with an empty set
                return new LinkedHashSet<>();
            }
            if (!(result.get(0) instanceof NitriteId)) {
                // a sub-map result means this is a compound-index level, not a single-field scan
                return null;
            }

            LinkedHashSet<NitriteId> ids = new LinkedHashSet<>((List<NitriteId>) result);
            if (acc == null) {
                acc = ids;
            } else {
                acc.retainAll(ids);
            }
            if (acc.isEmpty()) {
                break;
            }
        }

        return acc == null ? new LinkedHashSet<>() : acc;
    }

    private boolean isEmptyList(List<?> list) {
        return list == null || list.isEmpty();
    }

    private boolean isNitriteIdList(List<?> list) {
        Object value = list.get(0);
        return value instanceof NitriteId;
    }

    private boolean isNavigableMapList(List<?> list) {
        Object value = list.get(0);
        return value instanceof NavigableMap;
    }
}
