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

package org.dizitart.no2.index.fulltext.languages;

import org.dizitart.no2.index.fulltext.Language;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Sesotho stop words
 *
 * @since 2.1.0
 * @author Anindya Chatterjee
 */
public class Sesotho implements Language {
    @Override
    public Set<String> stopWords() {
        return new HashSet<>(Arrays.asList(
                "a",
                "ba",
                "bane",
                "bona",
                "e",
                "ea",
                "eaba",
                "empa",
                "ena",
                "ha",
                "hae",
                "hape",
                "ho",
                "hore",
                "ka",
                "ke",
                "la",
                "le",
                "li",
                "me",
                "mo",
                "moo",
                "ne",
                "o",
                "oa",
                "re",
                "sa",
                "se",
                "tloha",
                "tsa",
                "tse"
        ));
    }
}
