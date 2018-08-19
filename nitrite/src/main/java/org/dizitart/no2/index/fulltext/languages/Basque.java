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
 * Basque stop words
 *
 * @since 2.1.0
 * @author Anindya Chatterjee
 */
public class Basque implements Language {
    @Override
    public Set<String> stopWords() {
        return new HashSet<>(Arrays.asList(
                "al",
                "anitz",
                "arabera",
                "asko",
                "baina",
                "bat",
                "batean",
                "batek",
                "bati",
                "batzuei",
                "batzuek",
                "batzuetan",
                "batzuk",
                "bera",
                "beraiek",
                "berau",
                "berauek",
                "bere",
                "berori",
                "beroriek",
                "beste",
                "bezala",
                "da",
                "dago",
                "dira",
                "ditu",
                "du",
                "dute",
                "edo",
                "egin",
                "ere",
                "eta",
                "eurak",
                "ez",
                "gainera",
                "gu",
                "gutxi",
                "guzti",
                "haiei",
                "haiek",
                "haietan",
                "hainbeste",
                "hala",
                "han",
                "handik",
                "hango",
                "hara",
                "hari",
                "hark",
                "hartan",
                "hau",
                "hauei",
                "hauek",
                "hauetan",
                "hemen",
                "hemendik",
                "hemengo",
                "hi",
                "hona",
                "honek",
                "honela",
                "honetan",
                "honi",
                "hor",
                "hori",
                "horiei",
                "horiek",
                "horietan",
                "horko",
                "horra",
                "horrek",
                "horrela",
                "horretan",
                "horri",
                "hortik",
                "hura",
                "izan",
                "ni",
                "noiz",
                "nola",
                "non",
                "nondik",
                "nongo",
                "nor",
                "nora",
                "ze",
                "zein",
                "zen",
                "zenbait",
                "zenbat",
                "zer",
                "zergatik",
                "ziren",
                "zituen",
                "zu",
                "zuek",
                "zuen",
                "zuten"
        ));
    }
}
