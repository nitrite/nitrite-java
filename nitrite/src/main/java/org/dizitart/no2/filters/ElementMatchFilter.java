/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.filters;

import lombok.ToString;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.Filter;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.store.NitriteMap;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.*;
import static org.dizitart.no2.util.DocumentUtils.getFieldValue;
import static org.dizitart.no2.util.EqualsUtils.deepEquals;
import static org.dizitart.no2.util.NumberUtils.compare;

/**
 * @author Anindya Chatterjee.
 */
@ToString
class ElementMatchFilter extends BaseFilter {
    private String field;
    private Filter elementFilter;

    ElementMatchFilter(String field, Filter elementFilter) {
        this.field = field;
        this.elementFilter = elementFilter;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        if (elementFilter instanceof ElementMatchFilter) {
            throw new FilterException(NESTED_ELEM_MATCH_NOT_SUPPORTED);
        }

        if (elementFilter instanceof TextFilter) {
            throw new FilterException(FULL_TEXT_ELEM_MATCH_NOT_SUPPORTED);
        }

        elementFilter.setIndexedQueryTemplate(indexedQueryTemplate);

        Set<NitriteId> nitriteIdSet = new LinkedHashSet<>();
        for (Map.Entry<NitriteId, Document> entry: documentMap.entrySet()) {
            Document document = entry.getValue();
            Object fieldValue = getFieldValue(document, field);

            if (fieldValue == null) {
                continue;
            }

            if (fieldValue.getClass().isArray()) {
                int length = Array.getLength(fieldValue);
                List list = new ArrayList();
                for (int i = 0; i < length; i++) {
                    Object item = Array.get(fieldValue, i);
                    list.add(item);
                }

                if (matches(list, elementFilter)) {
                    nitriteIdSet.add(entry.getKey());
                }
            } else if (fieldValue instanceof Iterable) {
                if (matches((Iterable) fieldValue, elementFilter)) {
                    nitriteIdSet.add(entry.getKey());
                }
            } else {
                throw new FilterException(ELEM_MATCH_SUPPORTED_ON_ARRAY_ONLY);
            }
        }
        return nitriteIdSet;
    }

    private boolean matches(Iterable iterable, Filter filter) {
        for (Object item : iterable) {
            if (matchElement(item, filter)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean matchElement(Object item, Filter filter) {
        if (filter instanceof AndFilter) {
            Filter[] filters = ((AndFilter) filter).getFilters();
            for (Filter f : filters) {
                if (!matchElement(item, f)) {
                    return false;
                }
            }
            return true;
        } else if (filter instanceof OrFilter) {
            Filter[] filters = ((OrFilter) filter).getFilters();
            for (Filter f : filters) {
                if (matchElement(item, f)) {
                    return true;
                }
            }
            return false;
        } else if (filter instanceof NotFilter) {
            Filter not = ((NotFilter) filter).getFilter();
            return !matchElement(item, not);
        } else if (filter instanceof EqualsFilter) {
            return matchEqual(item, filter);
        } else if (filter instanceof GreaterEqualFilter) {
            return matchGreaterEqual(item, filter);
        } else if (filter instanceof GreaterThanFilter) {
            return matchGreater(item, filter);
        } else if (filter instanceof LesserEqualFilter) {
            return matchLesserEqual(item, filter);
        } else if (filter instanceof LesserThanFilter) {
            return matchLesser(item, filter);
        } else if (filter instanceof InFilter) {
            return matchIn(item, filter);
        } else if (filter instanceof RegexFilter) {
            return matchRegex(item, filter);
        } else {
            throw new FilterException(errorMessage("filter " + filter.getClass().getName() +
                    " is not a supported in elemMatch", FE_ELEM_MATCH_INVALID_FILTER));
        }
    }

    private boolean matchEqual(Object item, Filter filter) {
        Object value = ((EqualsFilter) filter).getValue();
        if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = getFieldValue(document, ((EqualsFilter) filter).getField());
            return deepEquals(value, docValue);
        } else {
            return deepEquals(item, value);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean matchGreater(Object item, Filter filter) {
        Comparable comparable = ((GreaterThanFilter) filter).getComparable();
        if (item instanceof Number && comparable instanceof Number) {
            return compare((Number) item, (Number) comparable) > 0;
        } else if (item instanceof Comparable) {
            Comparable arg = (Comparable) item;
            return arg.compareTo(comparable) > 0;
        } else if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = getFieldValue(document, ((GreaterThanFilter) filter).getField());
            if (docValue instanceof Comparable) {
                Comparable arg = (Comparable) docValue;
                return arg.compareTo(comparable) > 0;
            } else {
                throw new FilterException(errorMessage(
                        ((GreaterThanFilter) filter).getField() + " is not comparable",
                        FE_ELEM_MATCH_GT_FILTER_INVALID_FIELD));
            }
        } else {
            throw new FilterException(errorMessage(
                    item + " is not comparable",
                    FE_ELEM_MATCH_GT_FILTER_INVALID_ITEM));
        }
    }

    @SuppressWarnings("unchecked")
    private boolean matchGreaterEqual(Object item, Filter filter) {
        Comparable comparable = ((GreaterEqualFilter) filter).getComparable();
        if (item instanceof Number && comparable instanceof Number) {
            return compare((Number) item, (Number) comparable) >= 0;
        } else if (item instanceof Comparable) {
            Comparable arg = (Comparable) item;
            return arg.compareTo(comparable) >= 0;
        } else if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = getFieldValue(document, ((GreaterEqualFilter) filter).getField());
            if (docValue instanceof Comparable) {
                Comparable arg = (Comparable) docValue;
                return arg.compareTo(comparable) >= 0;
            } else {
                throw new FilterException(errorMessage(
                        ((GreaterEqualFilter) filter).getField() + " is not comparable",
                        FE_ELEM_MATCH_GTE_FILTER_INVALID_FIELD));
            }
        } else {
            throw new FilterException(errorMessage(
                    item + " is not comparable",
                    FE_ELEM_MATCH_GTE_FILTER_INVALID_ITEM));
        }
    }

    @SuppressWarnings("unchecked")
    private boolean matchLesserEqual(Object item, Filter filter) {
        Comparable comparable = ((LesserEqualFilter) filter).getComparable();
        if (item instanceof Number && comparable instanceof Number) {
            return compare((Number) item, (Number) comparable) <= 0;
        } else if (item instanceof Comparable) {
            Comparable arg = (Comparable) item;
            return arg.compareTo(comparable) <= 0;
        } else if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = getFieldValue(document, ((LesserEqualFilter) filter).getField());
            if (docValue instanceof Comparable) {
                Comparable arg = (Comparable) docValue;
                return arg.compareTo(comparable) <= 0;
            } else {
                throw new FilterException(errorMessage(
                        ((LesserEqualFilter) filter).getField() + " is not comparable",
                        FE_ELEM_MATCH_LTE_FILTER_INVALID_FIELD));
            }
        } else {
            throw new FilterException(errorMessage(
                    item + " is not comparable",
                    FE_ELEM_MATCH_LTE_FILTER_INVALID_ITEM));
        }
    }

    @SuppressWarnings("unchecked")
    private boolean matchLesser(Object item, Filter filter) {
        Comparable comparable = ((LesserThanFilter) filter).getComparable();
        if (item instanceof Number && comparable instanceof Number) {
            return compare((Number) item, (Number) comparable) < 0;
        } else if (item instanceof Comparable) {
            Comparable arg = (Comparable) item;
            return arg.compareTo(comparable) < 0;
        } else if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = getFieldValue(document, ((LesserThanFilter) filter).getField());
            if (docValue instanceof Comparable) {
                Comparable arg = (Comparable) docValue;
                return arg.compareTo(comparable) < 0;
            } else {
                throw new FilterException(errorMessage(
                        ((LesserThanFilter) filter).getField() + " is not comparable",
                        FE_ELEM_MATCH_LT_FILTER_INVALID_FIELD));
            }
        } else {
            throw new FilterException(errorMessage(
                    item + " is not comparable",
                    FE_ELEM_MATCH_LT_FILTER_INVALID_ITEM));
        }
    }

    private boolean matchIn(Object item, Filter filter) {
        List<Object> values = ((InFilter) filter).getObjectList();
        if (values != null) {
            if (item instanceof Document) {
                Document document = (Document) item;
                Object docValue = getFieldValue(document, ((InFilter) filter).getField());
                return values.contains(docValue);
            } else {
                return values.contains(item);
            }
        }
        return false;
    }

    private boolean matchRegex(Object item, Filter filter) {
        String value = ((RegexFilter) filter).getValue();
        if (item instanceof String) {
            Pattern pattern = Pattern.compile(value);
            Matcher matcher = pattern.matcher((String) item);
            return matcher.find();
        } else if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = getFieldValue(document, ((RegexFilter) filter).getField());
            if (docValue instanceof String) {
                Pattern pattern = Pattern.compile(value);
                Matcher matcher = pattern.matcher((String) docValue);
                return matcher.find();
            } else {
                throw new FilterException(errorMessage(
                        ((RegexFilter) filter).getField() + " is not a string",
                        FE_ELEM_MATCH_INVALID_REGEX));
            }
        } else {
            throw new FilterException(errorMessage(
                    item + " is not a string",
                    FE_ELEM_MATCH_REGEX_INVALID_ITEM));
        }
    }
}
