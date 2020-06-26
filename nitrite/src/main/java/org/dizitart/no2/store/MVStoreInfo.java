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

package org.dizitart.no2.store;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.KeyValuePair;

import java.util.HashMap;
import java.util.Map;

import static org.dizitart.no2.common.Constants.*;

/**
 * @since 4.0.0
 * @author Anindya Chatterjee
 */
class MVStoreInfo implements StoreInfo {
    private final Map<String, String> info;

    MVStoreInfo(Document document) {
        this.info = new HashMap<>();
        populateInfo(document);
    }

    @Override
    public Map<String, String> getInfo() {
        return info;
    }

    private void populateInfo(Document document) {
        document.remove(DOC_ID);
        document.remove(DOC_SOURCE);
        document.remove(DOC_REVISION);
        document.remove(DOC_MODIFIED);

        for (KeyValuePair<String, Object> keyValuePair : document) {
            info.put(keyValuePair.getKey(), keyValuePair.getValue().toString());
        }
    }
}
