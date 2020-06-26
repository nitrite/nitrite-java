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

package org.dizitart.no2.test;

import lombok.experimental.UtilityClass;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.Constants;
import org.junit.Assert;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author Anindya Chatterjee
 */
@UtilityClass
public class TestUtils {
    private static Random random = new Random();

    public static void assertEquals(NitriteCollection c1, NitriteCollection c2) {
        List<Document> l1 = c1.find().toList().stream().map(TestUtils::trimMeta).collect(Collectors.toList());
        List<Document> l2 = c2.find().toList().stream().map(TestUtils::trimMeta).collect(Collectors.toList());
        Assert.assertEquals(l1, l2);
    }

    public static void assertNotEquals(NitriteCollection c1, NitriteCollection c2) {
        List<Document> l1 = c1.find().toList().stream().map(TestUtils::trimMeta).collect(Collectors.toList());
        List<Document> l2 = c2.find().toList().stream().map(TestUtils::trimMeta).collect(Collectors.toList());
        Assert.assertNotEquals(l1, l2);
    }

    public static Document trimMeta(Document document) {
        document.remove(Constants.DOC_ID);
        document.remove(Constants.DOC_REVISION);
        document.remove(Constants.DOC_MODIFIED);
        document.remove(Constants.DOC_SOURCE);
        return document;
    }

    public static Document randomDocument() {
        return Document.createDocument()
            .put("firstName", randomString())
            .put("lastName", randomString())
            .put("age", random.nextInt());
    }

    private static String randomString() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;

        return random.ints(leftLimit, rightLimit + 1)
            .limit(targetStringLength)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }
}
