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
 * Marathi stop words
 *
 * @since 2.1.0
 * @author Anindya Chatterjee
 */
public class Marathi implements Language {
    @Override
    public Set<String> stopWords() {
        return new HashSet<>(Arrays.asList(
                "अधिक",
                "अनेक",
                "अशी",
                "असलयाचे",
                "असलेल्या",
                "असा",
                "असून",
                "असे",
                "आज",
                "आणि",
                "आता",
                "आपल्या",
                "आला",
                "आली",
                "आले",
                "आहे",
                "आहेत",
                "एक",
                "एका",
                "कमी",
                "करणयात",
                "करून",
                "का",
                "काम",
                "काय",
                "काही",
                "किवा",
                "की",
                "केला",
                "केली",
                "केले",
                "कोटी",
                "गेल्या",
                "घेऊन",
                "जात",
                "झाला",
                "झाली",
                "झाले",
                "झालेल्या",
                "टा",
                "डॉ",
                "तर",
                "तरी",
                "तसेच",
                "ता",
                "ती",
                "तीन",
                "ते",
                "तो",
                "त्या",
                "त्याचा",
                "त्याची",
                "त्याच्या",
                "त्याना",
                "त्यानी",
                "त्यामुळे",
                "त्री",
                "दिली",
                "दोन",
                "न",
                "नाही",
                "निर्ण्य",
                "पण",
                "पम",
                "परयतन",
                "पाटील",
                "म",
                "मात्र",
                "माहिती",
                "मी",
                "मुबी",
                "म्हणजे",
                "म्हणाले",
                "म्हणून",
                "या",
                "याचा",
                "याची",
                "याच्या",
                "याना",
                "यानी",
                "येणार",
                "येत",
                "येथील",
                "येथे",
                "लाख",
                "व",
                "व्यकत",
                "सर्व",
                "सागित्ले",
                "सुरू",
                "हजार",
                "हा",
                "ही",
                "हे",
                "होणार",
                "होत",
                "होता",
                "होती",
                "होते"
        ));
    }
}
