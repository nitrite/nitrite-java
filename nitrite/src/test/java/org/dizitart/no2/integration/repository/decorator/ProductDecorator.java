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

import org.dizitart.no2.repository.EntityIndex;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.EntityDecorator;
import org.dizitart.no2.repository.EntityId;

import java.util.Arrays;
import java.util.List;

public class ProductDecorator implements EntityDecorator<Product> {
    @Override
    public Class<Product> getEntityType() {
        return Product.class;
    }

    @Override
    public EntityId getIdField() {
        return new EntityId("productId", "uniqueId", "productCode");
    }

    @Override
    public List<EntityIndex> getIndexFields() {
        return Arrays.asList(
            new EntityIndex(IndexType.NON_UNIQUE, "manufacturer.name"),
            new EntityIndex(IndexType.UNIQUE, "productName", "manufacturer.uniqueId")
        );
    }

    @Override
    public String getEntityName() {
        return "product";
    }
}
