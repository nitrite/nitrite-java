/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.repository.data;

import lombok.Data;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anindya Chatterjee
 */
@Data
public class ElemMatch implements Mappable {
    private long id;
    private String[] strArray;
    private ProductScore[] productScores;

    @Override
    public Document write(NitriteMapper mapper) {
        List<Document> list = new ArrayList<>();
        if (productScores != null) {
            for (ProductScore productScore : productScores) {
                Document document = productScore.write(mapper);
                list.add(document);
            }
        }

        return Document.createDocument("id", id)
            .put("strArray", strArray)
            .put("productScores", list);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(NitriteMapper mapper, Document document) {
        id = document.get("id", Long.class);
        strArray = document.get("strArray", String[].class);
        List<Document> list = document.get("productScores", List.class);
        if (list != null) {
            productScores = new ProductScore[list.size()];
            for (int i = 0; i < list.size(); i++) {
                productScores[i] = new ProductScore();
                productScores[i].read(mapper, list.get(i));
            }
        }
    }
}
