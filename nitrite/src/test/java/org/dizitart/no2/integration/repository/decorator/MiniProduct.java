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

import lombok.Data;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;

@Data
public class MiniProduct {
    private String uniqueId;
    private String manufacturerName;
    private Double price;

    public static class Converter implements EntityConverter<MiniProduct> {

        @Override
        public Class<MiniProduct> getEntityType() {
            return MiniProduct.class;
        }

        @Override
        public Document toDocument(MiniProduct entity, NitriteMapper nitriteMapper) {
            return Document.createDocument()
                .put("productId.uniqueId", entity.getUniqueId())
                .put("manufacturer.name", entity.getManufacturerName())
                .put("price", entity.getPrice());
        }

        @Override
        public MiniProduct fromDocument(Document document, NitriteMapper nitriteMapper) {
            MiniProduct entity = new MiniProduct();
            entity.setUniqueId(document.get("productId.uniqueId", String.class));
            entity.setManufacturerName(document.get("manufacturer.name", String.class));
            entity.setPrice(document.get("price", Double.class));
            return entity;
        }
    }
}
