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
import org.dizitart.no2.collection.objects.ObjectFilter;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.store.NitriteMap;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
class ElemMatchObjectFilter extends BaseObjectFilter {
    private String field;
    private ObjectFilter elementFilter;

    ElemMatchObjectFilter(String field, ObjectFilter elementFilter) {
        this.field = field;
        this.elementFilter = elementFilter;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        if (elementFilter instanceof ElemMatchObjectFilter) {
            throw new FilterException(NESTED_OBJ_ELEM_MATCH_NOT_SUPPORTED);
        }

        if (elementFilter instanceof TextObjectFilter) {
            throw new FilterException(FULL_TEXT_OBJ_ELEM_MATCH_NOT_SUPPORTED);
        }

        elementFilter.setIndexedQueryTemplate(indexedQueryTemplate);
        elementFilter.setNitriteMapper(nitriteMapper);

        Set<NitriteId> nitriteIdSet = new LinkedHashSet<>();
        for (Map.Entry<NitriteId, Document> entry: documentMap.entrySet()) {
            Document document = entry.getValue();
            Object fieldValue = getFieldValue(document, field);

            if (fieldValue == null) {
                continue;
            }

            if (fieldValue instanceof Iterable) {
                if (matches((Iterable) fieldValue, elementFilter)) {
                    nitriteIdSet.add(entry.getKey());
                }
            } else {
                throw new FilterException(OBJ_ELEM_MATCH_SUPPORTED_ON_ARRAY_ONLY);
            }
        }
        return nitriteIdSet;
    }

    private boolean matches(Iterable iterable, ObjectFilter filter) {
        for (Object item : iterable) {
            if (matchElement(item, filter)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean matchElement(Object item, ObjectFilter filter) {
        if (filter instanceof AndObjectFilter) {
            ObjectFilter[] filters = ((AndObjectFilter) filter).getFilters();
            for (ObjectFilter f : filters) {
                if (!matchElement(item, f)) {
                    return false;
                }
            }
            return true;
        } else if (filter instanceof OrObjectFilter) {
            ObjectFilter[] filters = ((OrObjectFilter) filter).getFilters();
            for (ObjectFilter f : filters) {
                if (matchElement(item, f)) {
                    return true;
                }
            }
            return false;
        } else if (filter instanceof NotObjectFilter) {
            ObjectFilter not = ((NotObjectFilter) filter).getFilter();
            return !matchElement(item, not);
        } else if (filter instanceof EqualsObjectFilter) {
            return matchEqual(item, filter);
        } else if (filter instanceof GreaterEqualObjectFilter) {
            return matchGreaterEqual(item, filter);
        } else if (filter instanceof GreaterObjectFilter) {
            return matchGreater(item, filter);
        } else if (filter instanceof LesserEqualObjectFilter) {
            return matchLesserEqual(item, filter);
        } else if (filter instanceof LessThanObjectFilter) {
            return matchLesser(item, filter);
        } else if (filter instanceof InObjectFilter) {
            return matchIn(item, filter);
        } else if (filter instanceof RegexObjectFilter) {
            return matchRegex(item, filter);
        } else {
            throw new FilterException(errorMessage(
                    "filter " + filter.getClass().getName() + " is not a supported in elemMatch",
                    FE_OBJ_ELEM_MATCH_INVALID_FILTER));
        }
    }

    private boolean matchEqual(Object item, ObjectFilter filter) {
        Object value = ((EqualsObjectFilter) filter).getValue();
        if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = getFieldValue(document, ((EqualsObjectFilter) filter).getField());
            return deepEquals(value, docValue);
        } else {
            return deepEquals(item, value);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean matchGreater(Object item, ObjectFilter filter) {
        Object value = ((GreaterObjectFilter) filter).getValue();
        Comparable comparable;
        if (nitriteMapper.isValueType(value)) {
            comparable = (Comparable) nitriteMapper.asValue(value);
        } else {
            comparable = (Comparable) value;
        }
        if (item instanceof Number && comparable instanceof Number) {
            return compare((Number) item, (Number) comparable) > 0;
        } else if (item instanceof Comparable) {
            Comparable arg = (Comparable) item;
            return arg.compareTo(comparable) > 0;
        } else if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = getFieldValue(document, ((GreaterObjectFilter) filter).getField());
            if (docValue instanceof Comparable) {
                Comparable arg = (Comparable) docValue;
                return arg.compareTo(comparable) > 0;
            } else {
                throw new FilterException(errorMessage(
                        ((GreaterObjectFilter) filter).getField() + " is not comparable",
                        FE_OBJ_ELEM_MATCH_GT_FILTER_INVALID_FIELD));
            }
        } else {
            throw new FilterException(errorMessage(
                    item + " is not comparable",
                    FE_OBJ_ELEM_MATCH_GT_FILTER_INVALID_ITEM));
        }
    }

    @SuppressWarnings("unchecked")
    private boolean matchGreaterEqual(Object item, ObjectFilter filter) {
        Object value = ((GreaterEqualObjectFilter) filter).getValue();
        Comparable comparable;
        if (nitriteMapper.isValueType(value)) {
            comparable = (Comparable) nitriteMapper.asValue(value);
        } else {
            comparable = (Comparable) value;
        }
        if (item instanceof Number && comparable instanceof Number) {
            return compare((Number) item, (Number) comparable) >= 0;
        } else if (item instanceof Comparable) {
            Comparable arg = (Comparable) item;
            return arg.compareTo(comparable) >= 0;
        } else if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = getFieldValue(document, ((GreaterEqualObjectFilter) filter).getField());
            if (docValue instanceof Comparable) {
                Comparable arg = (Comparable) docValue;
                return arg.compareTo(comparable) >= 0;
            } else {
                throw new FilterException(errorMessage(
                        ((GreaterEqualObjectFilter) filter).getField() + " is not comparable",
                        FE_OBJ_ELEM_MATCH_GTE_FILTER_INVALID_FIELD));
            }
        } else {
            throw new FilterException(errorMessage(
                    item + " is not comparable",
                    FE_OBJ_ELEM_MATCH_GTE_FILTER_INVALID_ITEM));
        }
    }

    @SuppressWarnings("unchecked")
    private boolean matchLesserEqual(Object item, ObjectFilter filter) {
        Object value = ((LesserEqualObjectFilter) filter).getValue();
        Comparable comparable;
        if (nitriteMapper.isValueType(value)) {
            comparable = (Comparable) nitriteMapper.asValue(value);
        } else {
            comparable = (Comparable) value;
        }
        if (item instanceof Number && comparable instanceof Number) {
            return compare((Number) item, (Number) comparable) <= 0;
        } else if (item instanceof Comparable) {
            Comparable arg = (Comparable) item;
            return arg.compareTo(comparable) <= 0;
        } else if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = getFieldValue(document, ((LesserEqualObjectFilter) filter).getField());
            if (docValue instanceof Comparable) {
                Comparable arg = (Comparable) docValue;
                return arg.compareTo(comparable) <= 0;
            } else {
                throw new FilterException(errorMessage(
                        ((LesserEqualObjectFilter) filter).getField() + " is not comparable",
                        FE_OBJ_ELEM_MATCH_LTE_FILTER_INVALID_FIELD));
            }
        } else {
            throw new FilterException(errorMessage(
                    item + " is not comparable",
                    FE_OBJ_ELEM_MATCH_LTE_FILTER_INVALID_ITEM));
        }
    }

    @SuppressWarnings("unchecked")
    private boolean matchLesser(Object item, ObjectFilter filter) {
        Object value = ((LessThanObjectFilter) filter).getValue();
        Comparable comparable;
        if (nitriteMapper.isValueType(value)) {
            comparable = (Comparable) nitriteMapper.asValue(value);
        } else {
            comparable = (Comparable) value;
        }
        if (item instanceof Number && comparable instanceof Number) {
            return compare((Number) item, (Number) comparable) < 0;
        } else if (item instanceof Comparable) {
            Comparable arg = (Comparable) item;
            return arg.compareTo(comparable) < 0;
        } else if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = getFieldValue(document, ((LessThanObjectFilter) filter).getField());
            if (docValue instanceof Comparable) {
                Comparable arg = (Comparable) docValue;
                return arg.compareTo(comparable) < 0;
            } else {
                throw new FilterException(errorMessage(
                        ((LessThanObjectFilter) filter).getField() + " is not comparable",
                        FE_OBJ_ELEM_MATCH_LT_FILTER_INVALID_FIELD));
            }
        } else {
            throw new FilterException(errorMessage(
                    item + " is not comparable",
                    FE_OBJ_ELEM_MATCH_LT_FILTER_INVALID_ITEM));
        }
    }

    private boolean matchIn(Object item, ObjectFilter filter) {
        List<Object> values = ((InObjectFilter) filter).getObjectList();
        if (values != null) {
            if (item instanceof Document) {
                Document document = (Document) item;
                Object docValue = getFieldValue(document, ((InObjectFilter) filter).getField());
                return values.contains(docValue);
            } else {
                return values.contains(item);
            }
        }
        return false;
    }

    private boolean matchRegex(Object item, ObjectFilter filter) {
        String value = ((RegexObjectFilter) filter).getValue();
        if (item instanceof String) {
            Pattern pattern = Pattern.compile(value);
            Matcher matcher = pattern.matcher((String) item);
            return matcher.find();
        } else if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = getFieldValue(document, ((RegexObjectFilter) filter).getField());
            if (docValue instanceof String) {
                Pattern pattern = Pattern.compile(value);
                Matcher matcher = pattern.matcher((String) docValue);
                return matcher.find();
            } else {
                throw new FilterException(errorMessage(
                        ((RegexObjectFilter) filter).getField() + " is not a string",
                        FE_OBJ_ELEM_MATCH_INVALID_REGEX));
            }
        } else {
            throw new FilterException(errorMessage(
                    item + " is not comparable",
                    FE_OBJ_ELEM_MATCH_REGEX_INVALID_ITEM));
        }
    }
}
