package org.dizitart.no2.mapper;

import org.dizitart.no2.Document;

public interface MapperFacade {

	Document asDocument(Object object);

	<T> T asObject(Document document, Class<T> type);

	Object asValue(Object object);

	boolean isValueType(Object object);

	Document parse(String json);

	String toJson(Object object);

}
