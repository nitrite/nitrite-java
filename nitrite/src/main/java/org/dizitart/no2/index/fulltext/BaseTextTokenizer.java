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

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * An abstract text tokenizer which tokenizes a given string.
 * It discards certain words known as stop word depending on
 * the language chosen.
 *
 * @since 2.1.0
 * @author Anindya Chatterjee
 */
public abstract class BaseTextTokenizer implements TextTokenizer {
    private static final String WHITESPACE_CHARS = " \t\n\r\f+\"*%&/()=?'!,.;:-_#@|^~`{}[]<>\\";

    @Override
    public Set<String> tokenize(String text) {
        Set<String> words = new HashSet<>();
        StringTokenizer tokenizer = new StringTokenizer(text, WHITESPACE_CHARS);
        while (tokenizer.hasMoreTokens()) {
            String word = tokenizer.nextToken();
            word = convertWord(word);
            if (word != null) {
                words.add(word);
            }
        }
        return words;
    }

    /**
     * Converts a `word` into all lower case and checks if it
     * is a known stop word. If it is, then the `word` will be
     * discarded and will not be considered as a valid token.
     *
     * @param word the word
     * @return the tokenized word in all upper case.
     */
    protected String convertWord(String word) {
        word = word.toLowerCase();
        if (stopWords().contains(word)) {
            return null;
        }
        return word;
    }
}
