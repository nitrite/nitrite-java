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
 * Irish stop words
 *
 * @since 2.1.0
 * @author Anindya Chatterjee
 */
public class Irish implements Language {
    @Override
    public Set<String> stopWords() {
        return new HashSet<>(Arrays.asList(
                "a",
                "ach",
                "ag",
                "agus",
                "an",
                "aon",
                "ar",
                "arna",
                "as",
                "b'",
                "ba",
                "beirt",
                "bhúr",
                "caoga",
                "ceathair",
                "ceathrar",
                "chomh",
                "chtó",
                "chuig",
                "chun",
                "cois",
                "céad",
                "cúig",
                "cúigear",
                "d'",
                "daichead",
                "dar",
                "de",
                "deich",
                "deichniúr",
                "den",
                "dhá",
                "do",
                "don",
                "dtí",
                "dá",
                "dár",
                "dó",
                "faoi",
                "faoin",
                "faoina",
                "faoinár",
                "fara",
                "fiche",
                "gach",
                "gan",
                "go",
                "gur",
                "haon",
                "hocht",
                "i",
                "iad",
                "idir",
                "in",
                "ina",
                "ins",
                "inár",
                "is",
                "le",
                "leis",
                "lena",
                "lenár",
                "m'",
                "mar",
                "mo",
                "mé",
                "na",
                "nach",
                "naoi",
                "naonúr",
                "ná",
                "ní",
                "níor",
                "nó",
                "nócha",
                "ocht",
                "ochtar",
                "os",
                "roimh",
                "sa",
                "seacht",
                "seachtar",
                "seachtó",
                "seasca",
                "seisear",
                "siad",
                "sibh",
                "sinn",
                "sna",
                "sé",
                "sí",
                "tar",
                "thar",
                "thú",
                "triúr",
                "trí",
                "trína",
                "trínár",
                "tríocha",
                "tú",
                "um",
                "ár",
                "é",
                "éis",
                "í",
                "ó",
                "ón",
                "óna",
                "ónár"
        ));
    }
}
