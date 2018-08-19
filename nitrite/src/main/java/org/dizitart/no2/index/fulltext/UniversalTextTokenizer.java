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

import org.dizitart.no2.index.fulltext.languages.*;

import java.util.HashSet;
import java.util.Set;

/**
 * A {@link TextTokenizer} implementation for various languages.
 *
 * @see Languages
 * @author Anindya Chatterjee
 * @since 2.1.0
 */
public class UniversalTextTokenizer extends BaseTextTokenizer {
    private Set<String> stopWords = new HashSet<>();

    @Override
    public Set<String> stopWords() {
        return stopWords;
    }

    private void registerLanguage(Language language) {
        stopWords.addAll(language.stopWords());
    }

    public void loadLanguage(Languages... languages) {
        for (Languages language : languages) {
            switch (language) {
                case Afrikaans:
                    registerLanguage(new Afrikaans());
                    break;
                case Arabic:
                    registerLanguage(new Arabic());
                    break;
                case Armenian:
                    registerLanguage(new Armenian());
                    break;
                case Basque:
                    registerLanguage(new Basque());
                    break;
                case Bengali:
                    registerLanguage(new Bengali());
                    break;
                case Breton:
                    registerLanguage(new Breton());
                    break;
                case Bulgarian:
                    registerLanguage(new Bulgarian());
                    break;
                case Catalan:
                    registerLanguage(new Catalan());
                    break;
                case Chinese:
                    registerLanguage(new Chinese());
                    break;
                case Croatian:
                    registerLanguage(new Croatian());
                    break;
                case Czech:
                    registerLanguage(new Czech());
                    break;
                case Danish:
                    registerLanguage(new Danish());
                    break;
                case Dutch:
                    registerLanguage(new Dutch());
                    break;
                case English:
                    registerLanguage(new English());
                    break;
                case Esperanto:
                    registerLanguage(new Esperanto());
                    break;
                case Estonian:
                    registerLanguage(new Estonian());
                    break;
                case Finnish:
                    registerLanguage(new Finnish());
                    break;
                case French:
                    registerLanguage(new French());
                    break;
                case Galician:
                    registerLanguage(new Galician());
                    break;
                case German:
                    registerLanguage(new German());
                    break;
                case Greek:
                    registerLanguage(new Greek());
                    break;
                case Hausa:
                    registerLanguage(new Hausa());
                    break;
                case Hebrew:
                    registerLanguage(new Hebrew());
                    break;
                case Hindi:
                    registerLanguage(new Hindi());
                    break;
                case Hungarian:
                    registerLanguage(new Hungarian());
                    break;
                case Indonesian:
                    registerLanguage(new Indonesian());
                    break;
                case Irish:
                    registerLanguage(new Irish());
                    break;
                case Italian:
                    registerLanguage(new Italian());
                    break;
                case Japanese:
                    registerLanguage(new Japanese());
                    break;
                case Korean:
                    registerLanguage(new Korean());
                    break;
                case Kurdish:
                    registerLanguage(new Kurdish());
                    break;
                case Latin:
                    registerLanguage(new Latin());
                    break;
                case Latvian:
                    registerLanguage(new Latvian());
                    break;
                case Lithuanian:
                    registerLanguage(new Lithuanian());
                    break;
                case Malay:
                    registerLanguage(new Malay());
                    break;
                case Marathi:
                    registerLanguage(new Marathi());
                    break;
                case Norwegian:
                    registerLanguage(new Norwegian());
                    break;
                case Persian:
                    registerLanguage(new Persian());
                    break;
                case Polish:
                    registerLanguage(new Polish());
                    break;
                case Portuguese:
                    registerLanguage(new Portuguese());
                    break;
                case Romanian:
                    registerLanguage(new Romanian());
                    break;
                case Russian:
                    registerLanguage(new Russian());
                    break;
                case Sesotho:
                    registerLanguage(new Sesotho());
                    break;
                case Slovak:
                    registerLanguage(new Slovak());
                    break;
                case Slovenian:
                    registerLanguage(new Slovenian());
                    break;
                case Somali:
                    registerLanguage(new Somali());
                    break;
                case Spanish:
                    registerLanguage(new Spanish());
                    break;
                case Swahili:
                    registerLanguage(new Swahili());
                    break;
                case Swedish:
                    registerLanguage(new Swedish());
                    break;
                case Tagalog:
                    registerLanguage(new Tagalog());
                    break;
                case Thai:
                    registerLanguage(new Thai());
                    break;
                case Turkish:
                    registerLanguage(new Turkish());
                    break;
                case Ukrainian:
                    registerLanguage(new Ukrainian());
                    break;
                case Urdu:
                    registerLanguage(new Urdu());
                    break;
                case Vietnamese:
                    registerLanguage(new Vietnamese());
                    break;
                case Yoruba:
                    registerLanguage(new Yoruba());
                    break;
                case Zulu:
                    registerLanguage(new Zulu());
                    break;
            }
        }
    }

    public void loadAllLanguages() {
        loadLanguage(Languages.values());
    }
}
