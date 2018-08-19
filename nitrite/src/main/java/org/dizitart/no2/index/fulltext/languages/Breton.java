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
 * Breton stop words
 *
 * @since 2.1.0
 * @author Anindya Chatterjee
 */
public class Breton implements Language {
    @Override
    public Set<String> stopWords() {
        return new HashSet<>(Arrays.asList(
                "a",
                "ainda",
                "alem",
                "ambas",
                "ambos",
                "antes",
                "ao",
                "aonde",
                "aos",
                "apos",
                "aquele",
                "aqueles",
                "as",
                "assim",
                "com",
                "como",
                "contra",
                "contudo",
                "cuja",
                "cujas",
                "cujo",
                "cujos",
                "da",
                "das",
                "de",
                "dela",
                "dele",
                "deles",
                "demais",
                "depois",
                "desde",
                "desta",
                "deste",
                "dispoe",
                "dispoem",
                "diversa",
                "diversas",
                "diversos",
                "do",
                "dos",
                "durante",
                "e",
                "ela",
                "elas",
                "ele",
                "eles",
                "em",
                "entao",
                "entre",
                "essa",
                "essas",
                "esse",
                "esses",
                "esta",
                "estas",
                "este",
                "estes",
                "ha",
                "isso",
                "isto",
                "logo",
                "mais",
                "mas",
                "mediante",
                "menos",
                "mesma",
                "mesmas",
                "mesmo",
                "mesmos",
                "na",
                "nao",
                "nas",
                "nem",
                "nesse",
                "neste",
                "nos",
                "o",
                "os",
                "ou",
                "outra",
                "outras",
                "outro",
                "outros",
                "pelas",
                "pelo",
                "pelos",
                "perante",
                "pois",
                "por",
                "porque",
                "portanto",
                "propios",
                "proprio",
                "quais",
                "qual",
                "qualquer",
                "quando",
                "quanto",
                "que",
                "quem",
                "quer",
                "se",
                "seja",
                "sem",
                "sendo",
                "seu",
                "seus",
                "sob",
                "sobre",
                "sua",
                "suas",
                "tal",
                "tambem",
                "teu",
                "teus",
                "toda",
                "todas",
                "todo",
                "todos",
                "tua",
                "tuas",
                "tudo",
                "um",
                "uma",
                "umas",
                "uns"
        ));
    }
}
