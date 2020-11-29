package org.dizitart.no2.common;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * @author Anindya Chatterjee
 */
@Getter @Setter
public class FieldNames extends AbstractSet<String> {
    private List<String> names;

    public FieldNames() {
        names = new ArrayList<>();
    }

    public FieldNames(Collection<String> collection) {
        names = new ArrayList<>();
        addAll(collection);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldNames)) return false;
        FieldNames that = (FieldNames) o;
        return Objects.equals(names, that.names);
    }

    @Override
    public int hashCode() {
        return Objects.hash(names);
    }

    @Override
    public Iterator<String> iterator() {
        return names.listIterator();
    }

    @Override
    public int size() {
        return names.size();
    }

    @Override
    public boolean isEmpty() {
        return names.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return names.contains(o);
    }

    @Override
    public boolean add(String s) {
        names.remove(s);
        return names.add(s);
    }

    @Override
    public boolean remove(Object o) {
        return names.remove(o);
    }

    @Override
    public void clear() {
        names.clear();
    }

    @Override
    public String toString() {
        return names.toString();
    }

    public String get(int index) {
        return names.get(index);
    }
}
