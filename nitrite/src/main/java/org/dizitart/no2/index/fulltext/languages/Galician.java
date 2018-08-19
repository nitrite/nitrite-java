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
 * Galiciam stop words
 *
 * @since 2.1.0
 * @author Anindya Chatterjee
 */
public class Galician implements Language {
    @Override
    public Set<String> stopWords() {
        return new HashSet<>(Arrays.asList(
                "a",
                "alí",
                "ao",
                "aos",
                "aquel",
                "aquela",
                "aquelas",
                "aqueles",
                "aquilo",
                "aquí",
                "as",
                "así",
                "aínda",
                "ben",
                "cando",
                "che",
                "co",
                "coa",
                "coas",
                "comigo",
                "con",
                "connosco",
                "contigo",
                "convosco",
                "cos",
                "cun",
                "cunha",
                "cunhas",
                "cuns",
                "da",
                "dalgunha",
                "dalgunhas",
                "dalgún",
                "dalgúns",
                "das",
                "de",
                "del",
                "dela",
                "delas",
                "deles",
                "desde",
                "deste",
                "do",
                "dos",
                "dun",
                "dunha",
                "dunhas",
                "duns",
                "e",
                "el",
                "ela",
                "elas",
                "eles",
                "en",
                "era",
                "eran",
                "esa",
                "esas",
                "ese",
                "eses",
                "esta",
                "estaba",
                "estar",
                "este",
                "estes",
                "estiven",
                "estou",
                "está",
                "están",
                "eu",
                "facer",
                "foi",
                "foron",
                "fun",
                "había",
                "hai",
                "iso",
                "isto",
                "la",
                "las",
                "lle",
                "lles",
                "lo",
                "los",
                "mais",
                "me",
                "meu",
                "meus",
                "min",
                "miña",
                "miñas",
                "moi",
                "na",
                "nas",
                "neste",
                "nin",
                "no",
                "non",
                "nos",
                "nosa",
                "nosas",
                "noso",
                "nosos",
                "nun",
                "nunha",
                "nunhas",
                "nuns",
                "nós",
                "o",
                "os",
                "ou",
                "para",
                "pero",
                "pode",
                "pois",
                "pola",
                "polas",
                "polo",
                "polos",
                "por",
                "que",
                "se",
                "senón",
                "ser",
                "seu",
                "seus",
                "sexa",
                "sido",
                "sobre",
                "súa",
                "súas",
                "tamén",
                "tan",
                "te",
                "ten",
                "ter",
                "teu",
                "teus",
                "teñen",
                "teño",
                "ti",
                "tido",
                "tiven",
                "tiña",
                "túa",
                "túas",
                "un",
                "unha",
                "unhas",
                "uns",
                "vos",
                "vosa",
                "vosas",
                "voso",
                "vosos",
                "vós",
                "á",
                "é",
                "ó",
                "ós"
        ));
    }
}
