package org.dizitart.no2.tool;

import org.dizitart.no2.Document;

public interface JsonDeSerializer {

	Document parse(String json);

	String toJson(Object object);

}
