package org.dizitart.nitrite.test.repository.model;

import java.util.Objects;
import java.util.Set;

import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;
import org.dizitart.no2.repository.annotations.Indices;

// @Indices requires additional reflection hints for GraalVM
// see: src/test/resources/META-INF/native-image/org.dizitart/nitrite-native-tests/reflect-config.json
@Indices(
    @Index(fields = "lastName", type = IndexType.NON_UNIQUE)
)
public class Person {
    @Id
    private final String id;
    private final String firstName;
    private final String lastName;
    private final int age;
    private final Set<Title> titles;

    // additional serialization hints are required to serialize boolean fields
    // this also applies to other Serializable classes
    // see: src/test/resources/META-INF/native-image/org.dizitart/nitrite-native-tests/serialization-config.json
    private final boolean someBoolean;

    public Person(final String id,
                  final String firstName,
                  final String lastName,
                  final int age,
                  final Set<Title> titles,
                  final boolean someBoolean) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.titles = titles;
        this.someBoolean = someBoolean;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getAge() {
        return age;
    }

    public Set<Title> getTitles() {
        return titles;
    }

    public boolean isSomeBoolean() {
        return someBoolean;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Person person = (Person) o;
        return age == person.age && someBoolean == person.someBoolean && Objects.equals(id, person.id) && Objects.equals(firstName, person.firstName) && Objects.equals(lastName, person.lastName) && Objects.equals(titles, person.titles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, age, titles, someBoolean);
    }
}
