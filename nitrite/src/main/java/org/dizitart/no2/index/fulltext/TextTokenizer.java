/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.index.fulltext;

import org.dizitart.no2.index.TextIndexer;

import java.io.IOException;
import java.util.Set;

/**
 * A stop-word based string tokenizer.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 * @see TextIndexer
 * @see EnglishTextTokenizer
 * @see org.dizitart.no2.NitriteBuilder#textTokenizer(TextTokenizer)
 */
public interface TextTokenizer {
    /**
     * Tokenize a `text` and discards all stop-words from it.
     *
     * @param text the text to tokenize
     * @return the set of tokens.
     * @throws IOException if a low-level I/O error occurs.
     */
    Set<String> tokenize(String text) throws IOException;

    /**
     * Gets all stop-words for a language.
     *
     * @return the set of all stop-words.
     */
    Set<String> stopWords();
}
