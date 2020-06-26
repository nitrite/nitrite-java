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

package org.dizitart.no2.rx;

import io.reactivex.Single;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.UpdateOptions;
import org.dizitart.no2.filters.Filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.dizitart.no2.common.util.ValidationUtils.containsNull;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee
 */
public interface RxNitriteCollection extends RxPersistentCollection<Document> {
    default FlowableWriteResult insert(Document document, Document... documents) {
        notNull(document, "a null document cannot be inserted");
        if (documents != null) {
            containsNull(documents, "a null document cannot be inserted");
        }

        List<Document> documentList = new ArrayList<>();
        documentList.add(document);

        if (documents != null && documents.length > 0) {
            Collections.addAll(documentList, documents);
        }

        return insert(documentList.toArray(new Document[0]));
    }

    default FlowableWriteResult update(Filter filter, Document update) {
        return update(filter, update, new UpdateOptions());
    }

    FlowableWriteResult update(Filter filter, Document update, UpdateOptions updateOptions);

    default FlowableWriteResult remove(Filter filter) {
        return remove(filter, false);
    }

    FlowableWriteResult remove(Filter filter, boolean justOne);

    FlowableDocumentCursor find();

    FlowableDocumentCursor find(Filter filter);

    Single<Document> getById(NitriteId nitriteId);

    String getName();
}
