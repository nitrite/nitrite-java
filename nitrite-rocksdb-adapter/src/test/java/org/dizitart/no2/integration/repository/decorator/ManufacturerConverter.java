/*
 * Copyright (c) 2017-2022 Nitrite author or authors.
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

package org.dizitart.no2.integration.repository.decorator;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;

public class ManufacturerConverter implements EntityConverter<Manufacturer> {
    @Override
    public Class<Manufacturer> getEntityType() {
        return Manufacturer.class;
    }

    @Override
    public Document toDocument(Manufacturer entity, NitriteMapper nitriteMapper) {
        return Document.createDocument("name", entity.getName())
            .put("address", entity.getAddress())
            .put("uniqueId", entity.getUniqueId());
    }

    @Override
    public Manufacturer fromDocument(Document document, NitriteMapper nitriteMapper) {
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setName(document.get("name", String.class));
        manufacturer.setAddress(document.get("address", String.class));
        manufacturer.setUniqueId(document.get("uniqueId", Integer.class));
        return manufacturer;
    }
}
