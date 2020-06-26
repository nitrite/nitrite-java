/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.filters;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.exceptions.FilterException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.dizitart.no2.common.util.NumberUtils.compare;
import static org.dizitart.no2.common.util.ObjectUtils.deepEquals;

/**
 * @author Anindya Chatterjee
 */
class ElementMatchFilter extends NitriteFilter {
    private final String field;
    private final Filter elementFilter;

    ElementMatchFilter(String field, Filter elementFilter) {
        this.elementFilter = elementFilter;
        this.field = field;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public boolean apply(KeyValuePair<NitriteId, Document> element) {
        if (elementFilter instanceof ElementMatchFilter) {
            throw new FilterException("nested elemMatch filter is not supported");
        }

        if (elementFilter instanceof TextFilter) {
            throw new FilterException("text filter is not supported in elemMatch filter");
        }

        Document document = element.getValue();
        Object fieldValue = document.get(field);
        if (fieldValue == null) {
            return false;
        }

        if (fieldValue.getClass().isArray()) {
            int length = Array.getLength(fieldValue);
            List list = new ArrayList(length);
            for (int i = 0; i < length; i++) {
                Object item = Array.get(fieldValue, i);
                list.add(item);
            }

            return matches(list, elementFilter);
        } else if (fieldValue instanceof Iterable) {
            return matches((Iterable) fieldValue, elementFilter);
        } else {
            throw new FilterException("elemMatch filter only applies to array or iterable");
        }
    }

    @SuppressWarnings("rawtypes")
    private boolean matches(Iterable iterable, Filter filter) {
        for (Object item : iterable) {
            if (matchElement(item, filter)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchElement(Object item, Filter filter) {
        if (filter instanceof AndFilter) {
            List<Filter> filters = ((AndFilter) filter).getFilters();
            for (Filter f : filters) {
                if (!matchElement(item, f)) {
                    return false;
                }
            }
            return true;
        } else if (filter instanceof OrFilter) {
            List<Filter> filters = ((OrFilter) filter).getFilters();
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
        } else if (filter instanceof NotInFilter) {
            return matchNotIn(item, filter);
        } else if (filter instanceof RegexFilter) {
            return matchRegex(item, filter);
        } else {
            throw new FilterException("filter " + filter.getClass().getSimpleName() +
                " is not a supported in elemMatch");
        }
    }

    private boolean matchEqual(Object item, Filter filter) {
        Object value = ((EqualsFilter) filter).getValue();
        if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = document.get(((EqualsFilter) filter).getField());
            return deepEquals(value, docValue);
        } else {
            return deepEquals(item, value);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean matchGreater(Object item, Filter filter) {
        Comparable comparable = ((GreaterThanFilter) filter).getComparable();

        if (item instanceof Number && comparable instanceof Number) {
            return compare((Number) item, (Number) comparable) > 0;
        } else if (item instanceof Comparable) {
            Comparable arg = (Comparable) item;
            return arg.compareTo(comparable) > 0;
        } else if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = document.get(((GreaterThanFilter) filter).getField());
            if (docValue instanceof Comparable) {
                Comparable arg = (Comparable) docValue;
                return arg.compareTo(comparable) > 0;
            } else {
                throw new FilterException(
                    ((GreaterThanFilter) filter).getField() + " is not comparable");
            }
        } else {
            throw new FilterException(item + " is not comparable");
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean matchGreaterEqual(Object item, Filter filter) {
        Comparable comparable = ((GreaterEqualFilter) filter).getComparable();

        if (item instanceof Number && comparable instanceof Number) {
            return compare((Number) item, (Number) comparable) >= 0;
        } else if (item instanceof Comparable) {
            Comparable arg = (Comparable) item;
            return arg.compareTo(comparable) >= 0;
        } else if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = document.get(((GreaterEqualFilter) filter).getField());
            if (docValue instanceof Comparable) {
                Comparable arg = (Comparable) docValue;
                return arg.compareTo(comparable) >= 0;
            } else {
                throw new FilterException(((GreaterEqualFilter) filter).getField() + " is not comparable");
            }
        } else {
            throw new FilterException(item + " is not comparable");
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean matchLesserEqual(Object item, Filter filter) {
        Comparable comparable = ((LesserEqualFilter) filter).getComparable();

        if (item instanceof Number && comparable instanceof Number) {
            return compare((Number) item, (Number) comparable) <= 0;
        } else if (item instanceof Comparable) {
            Comparable arg = (Comparable) item;
            return arg.compareTo(comparable) <= 0;
        } else if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = document.get(((LesserEqualFilter) filter).getField());
            if (docValue instanceof Comparable) {
                Comparable arg = (Comparable) docValue;
                return arg.compareTo(comparable) <= 0;
            } else {
                throw new FilterException(((LesserEqualFilter) filter).getField() + " is not comparable");
            }
        } else {
            throw new FilterException(item + " is not comparable");
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean matchLesser(Object item, Filter filter) {
        Comparable comparable = ((LesserThanFilter) filter).getComparable();

        if (item instanceof Number && comparable instanceof Number) {
            return compare((Number) item, (Number) comparable) < 0;
        } else if (item instanceof Comparable) {
            Comparable arg = (Comparable) item;
            return arg.compareTo(comparable) < 0;
        } else if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = document.get(((LesserThanFilter) filter).getField());
            if (docValue instanceof Comparable) {
                Comparable arg = (Comparable) docValue;
                return arg.compareTo(comparable) < 0;
            } else {
                throw new FilterException(((LesserThanFilter) filter).getField() + " is not comparable");
            }
        } else {
            throw new FilterException(item + " is not comparable");
        }
    }

    @SuppressWarnings("rawtypes")
    private boolean matchIn(Object item, Filter filter) {
        Set<Comparable> values = ((InFilter) filter).getComparableSet();
        if (values != null) {
            if (item instanceof Document) {
                Document document = (Document) item;
                Object docValue = document.get(((InFilter) filter).getField());
                if (docValue instanceof Comparable) {
                    return values.contains(docValue);
                }
            } else if (item instanceof Comparable) {
                return values.contains(item);
            }
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    private boolean matchNotIn(Object item, Filter filter) {
        Set<Comparable> values = ((NotInFilter) filter).getComparableSet();
        if (values != null) {
            if (item instanceof Document) {
                Document document = (Document) item;
                Object docValue = document.get(((NotInFilter) filter).getField());
                if (docValue instanceof Comparable) {
                    return !values.contains(docValue);
                }
            } else if (item instanceof Comparable) {
                return !values.contains(item);
            }
        }
        return false;
    }

    private boolean matchRegex(Object item, Filter filter) {
        String value = (String) ((RegexFilter) filter).getValue();
        if (item instanceof String) {
            Pattern pattern = Pattern.compile(value);
            Matcher matcher = pattern.matcher((String) item);
            return matcher.find();
        } else if (item instanceof Document) {
            Document document = (Document) item;
            Object docValue = document.get(((RegexFilter) filter).getField());
            if (docValue instanceof String) {
                Pattern pattern = Pattern.compile(value);
                Matcher matcher = pattern.matcher((String) docValue);
                return matcher.find();
            } else {
                throw new FilterException(((RegexFilter) filter).getField() + " is not a string");
            }
        } else {
            throw new FilterException(item + " is not a string");
        }
    }
}
