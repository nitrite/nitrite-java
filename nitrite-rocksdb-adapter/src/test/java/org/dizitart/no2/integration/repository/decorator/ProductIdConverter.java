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

public class ProductIdConverter implements EntityConverter<ProductId> {
    @Override
    public Class<ProductId> getEntityType() {
        return ProductId.class;
    }

    @Override
    public Document toDocument(ProductId entity, NitriteMapper nitriteMapper) {
        return Document.createDocument("uniqueId", entity.getUniqueId())
            .put("productCode", entity.getProductCode());
    }

    @Override
    public ProductId fromDocument(Document document, NitriteMapper nitriteMapper) {
        ProductId entity = new ProductId();
        entity.setUniqueId(document.get("uniqueId", String.class));
        entity.setProductCode(document.get("productCode", String.class));
        return entity;
    }
}
