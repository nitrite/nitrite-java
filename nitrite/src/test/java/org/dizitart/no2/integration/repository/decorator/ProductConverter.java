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

public class ProductConverter implements EntityConverter<Product> {
    @Override
    public Class<Product> getEntityType() {
        return Product.class;
    }

    @Override
    public Document toDocument(Product entity, NitriteMapper nitriteMapper) {
        Document productId = (Document) nitriteMapper.tryConvert(entity.getProductId(), Document.class);
        Document manufacturer = (Document) nitriteMapper.tryConvert(entity.getManufacturer(), Document.class);

        return Document.createDocument()
            .put("productId", productId)
            .put("manufacturer", manufacturer)
            .put("productName", entity.getProductName())
            .put("price", entity.getPrice());
    }

    @Override
    public Product fromDocument(Document document, NitriteMapper nitriteMapper) {
        Product entity = new Product();
        ProductId productId = (ProductId) nitriteMapper.tryConvert(document.get("productId", Document.class), ProductId.class);
        Manufacturer manufacturer = (Manufacturer) nitriteMapper.tryConvert(document.get("manufacturer", Document.class),
            Manufacturer.class);
        entity.setProductId(productId);
        entity.setManufacturer(manufacturer);
        entity.setProductName(document.get("productName", String.class));
        entity.setPrice(document.get("price", Double.class));
        return entity;
    }
}
