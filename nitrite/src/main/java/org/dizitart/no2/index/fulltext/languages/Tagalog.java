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
 * Tagalog stop words
 *
 * @since 2.1.0
 * @author Anindya Chatterjee
 */
public class Tagalog implements Language {
    @Override
    public Set<String> stopWords() {
        return new HashSet<>(Arrays.asList(
                "akin",
                "aking",
                "ako",
                "alin",
                "am",
                "amin",
                "aming",
                "ang",
                "ano",
                "anumang",
                "apat",
                "at",
                "atin",
                "ating",
                "ay",
                "bababa",
                "bago",
                "bakit",
                "bawat",
                "bilang",
                "dahil",
                "dalawa",
                "dapat",
                "din",
                "dito",
                "doon",
                "gagawin",
                "gayunman",
                "ginagawa",
                "ginawa",
                "ginawang",
                "gumawa",
                "gusto",
                "habang",
                "hanggang",
                "hindi",
                "huwag",
                "iba",
                "ibaba",
                "ibabaw",
                "ibig",
                "ikaw",
                "ilagay",
                "ilalim",
                "ilan",
                "inyong",
                "isa",
                "isang",
                "itaas",
                "ito",
                "iyo",
                "iyon",
                "iyong",
                "ka",
                "kahit",
                "kailangan",
                "kailanman",
                "kami",
                "kanila",
                "kanilang",
                "kanino",
                "kanya",
                "kanyang",
                "kapag",
                "kapwa",
                "karamihan",
                "katiyakan",
                "katulad",
                "kaya",
                "kaysa",
                "ko",
                "kong",
                "kulang",
                "kumuha",
                "kung",
                "laban",
                "lahat",
                "lamang",
                "likod",
                "lima",
                "maaari",
                "maaaring",
                "maging",
                "mahusay",
                "makita",
                "marami",
                "marapat",
                "masyado",
                "may",
                "mayroon",
                "mga",
                "minsan",
                "mismo",
                "mula",
                "muli",
                "na",
                "nabanggit",
                "naging",
                "nagkaroon",
                "nais",
                "nakita",
                "namin",
                "napaka",
                "narito",
                "nasaan",
                "ng",
                "ngayon",
                "ni",
                "nila",
                "nilang",
                "nito",
                "niya",
                "niyang",
                "noon",
                "o",
                "pa",
                "paano",
                "pababa",
                "paggawa",
                "pagitan",
                "pagkakaroon",
                "pagkatapos",
                "palabas",
                "pamamagitan",
                "panahon",
                "pangalawa",
                "para",
                "paraan",
                "pareho",
                "pataas",
                "pero",
                "pumunta",
                "pumupunta",
                "sa",
                "saan",
                "sabi",
                "sabihin",
                "sarili",
                "sila",
                "sino",
                "siya",
                "tatlo",
                "tayo",
                "tulad",
                "tungkol",
                "una",
                "walang"
        ));
    }
}
