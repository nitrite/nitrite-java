package org.dizitart.nitrite.test.repository;

import java.util.Set;
import java.util.stream.Collectors;

import org.dizitart.nitrite.test.repository.model.Person;
import org.dizitart.nitrite.test.repository.model.Title;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;

public class PersonEntityConverter implements EntityConverter<Person> {

    @Override
    public Class<Person> getEntityType() {
        return Person.class;
    }

    @Override
    public Document toDocument(final Person person, final NitriteMapper nitriteMapper) {
        return Document.createDocument()
            .put("id", person.getId())
            .put("firstName", person.getFirstName())
            .put("lastName", person.getLastName())
            .put("age", person.getAge())
            .put("titles", person.getTitles().stream().map(Enum::name).collect(Collectors.toSet()))
            .put("someBoolean", person.isSomeBoolean());
    }

    @Override
    public Person fromDocument(final Document document, final NitriteMapper nitriteMapper) {
        return new Person(
            document.get("id", String.class),
            document.get("firstName", String.class),
            document.get("lastName", String.class),
            (int) document.get("age"),
            ((Set<?>) document.get("titles")).stream().map(title -> Title.valueOf((String) title)).collect(Collectors.toSet()),
            (boolean) document.get("someBoolean")
        );
    }
}
