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

import java.util.Set;

/**
 * A stop-word based string tokenizer.
 *
 * @author Anindya Chatterjee.
 * @see EnglishTextTokenizer
 * @since 1.0
 */
public interface TextTokenizer {

    /**
     * Gets the language for the tokenizer.
     *
     * @return the language for this tokenizer.
     */
    Languages getLanguage();

    /**
     * Tokenize a <code>text</code> and discards all stop-words from it.
     *
     * @param text the text to tokenize
     * @return the set of tokens.
     */
    Set<String> tokenize(String text);

    /**
     * Gets all stop-words for a language.
     *
     * @return the set of all stop-words.
     */
    Set<String> stopWords();
}
