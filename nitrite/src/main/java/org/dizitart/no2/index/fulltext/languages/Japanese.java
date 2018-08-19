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
 * Japanese stop words
 *
 * @since 2.1.0
 * @author Anindya Chatterjee
 */
public class Japanese implements Language {
    @Override
    public Set<String> stopWords() {
        return new HashSet<>(Arrays.asList(
                "あそこ",
                "あっ",
                "あの",
                "あのかた",
                "あの人",
                "あり",
                "あります",
                "ある",
                "あれ",
                "い",
                "いう",
                "います",
                "いる",
                "う",
                "うち",
                "え",
                "お",
                "および",
                "おり",
                "おります",
                "か",
                "かつて",
                "から",
                "が",
                "き",
                "ここ",
                "こちら",
                "こと",
                "この",
                "これ",
                "これら",
                "さ",
                "さらに",
                "し",
                "しかし",
                "する",
                "ず",
                "せ",
                "せる",
                "そこ",
                "そして",
                "その",
                "その他",
                "その後",
                "それ",
                "それぞれ",
                "それで",
                "た",
                "ただし",
                "たち",
                "ため",
                "たり",
                "だ",
                "だっ",
                "だれ",
                "つ",
                "て",
                "で",
                "でき",
                "できる",
                "です",
                "では",
                "でも",
                "と",
                "という",
                "といった",
                "とき",
                "ところ",
                "として",
                "とともに",
                "とも",
                "と共に",
                "どこ",
                "どの",
                "な",
                "ない",
                "なお",
                "なかっ",
                "ながら",
                "なく",
                "なっ",
                "など",
                "なに",
                "なら",
                "なり",
                "なる",
                "なん",
                "に",
                "において",
                "における",
                "について",
                "にて",
                "によって",
                "により",
                "による",
                "に対して",
                "に対する",
                "に関する",
                "の",
                "ので",
                "のみ",
                "は",
                "ば",
                "へ",
                "ほか",
                "ほとんど",
                "ほど",
                "ます",
                "また",
                "または",
                "まで",
                "も",
                "もの",
                "ものの",
                "や",
                "よう",
                "より",
                "ら",
                "られ",
                "られる",
                "れ",
                "れる",
                "を",
                "ん",
                "何",
                "及び",
                "彼",
                "彼女",
                "我々",
                "特に",
                "私",
                "私達",
                "貴方",
                "貴方方"
        ));
    }
}
