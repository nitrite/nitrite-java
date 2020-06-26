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

package org.dizitart.no2.index.fulltext;

import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author Anindya Chatterjee
 */
public class TokenizerTests {
    @Test
    public void testTokenize() throws IOException {
        TextTokenizer tokenizer = new BaseTextTokenizer() {
            @Override
            public Languages getLanguage() {
                return Languages.ALL;
            }

            @Override
            public Set<String> stopWords() {
                return Collections.emptySet();
            }

            @Override
            protected String convertWord(String word) {
                return word;
            }
        };

        String text = "A    B+C\tD\nE\rF\fG\"H*I%J&K/L(M)N=O?P'Q!R,S.T;U:V-W_X#Y@Z|a^b~c`d{e}f[g]h<i>j\\k";
        Set<String> stringSet = tokenizer.tokenize(text);

        Set<String> results = new HashSet<>();
        Collections.addAll(results,
            "A", "B", "C", "D", "E", "F", "G",
            "H", "I", "J", "K", "L", "M", "N",
            "O", "P", "Q", "R", "S", "T", "U",
            "V", "W", "X", "Y", "Z", "a", "b",
            "c", "d", "e", "f", "g", "h", "i",
            "j", "k");

        assertArrayEquals(results.toArray(), stringSet.toArray());
    }
}
