/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
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

package org.dizitart.no2.integration.repository.data;

import lombok.Data;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anindya Chatterjee
 */
@Data
public class ElemMatch {
    private Long id;
    private String[] strArray;
    private ProductScore[] productScores;

    public static class Converter implements EntityConverter<ElemMatch> {

        @Override
        public Class<ElemMatch> getEntityType() {
            return ElemMatch.class;
        }

        @Override
        public Document toDocument(ElemMatch entity, NitriteMapper nitriteMapper) {
            List<Document> list = new ArrayList<>();
            if (entity.productScores != null) {
                for (ProductScore productScore : entity.productScores) {
                    Document document = (Document) nitriteMapper.tryConvert(productScore, Document.class);
                    list.add(document);
                }
            }

            return Document.createDocument("id", entity.id)
                .put("strArray", entity.strArray)
                .put("productScores", list);
        }

        @Override
        @SuppressWarnings("unchecked")
        public ElemMatch fromDocument(Document document, NitriteMapper nitriteMapper) {
            ElemMatch entity = new ElemMatch();
            entity.id = document.get("id", Long.class);
            entity.strArray = document.get("strArray", String[].class);
            List<Document> list = document.get("productScores", List.class);
            if (list != null) {
                entity.productScores = new ProductScore[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    entity.productScores[i] = (ProductScore) nitriteMapper.tryConvert(list.get(i), ProductScore.class);
                }
            }
            return entity;
        }
    }
}
