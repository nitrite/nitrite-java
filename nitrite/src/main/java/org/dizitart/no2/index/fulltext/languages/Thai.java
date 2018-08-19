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
 * Thai stop words
 *
 * @since 2.1.0
 * @author Anindya Chatterjee
 */
public class Thai implements Language {
    @Override
    public Set<String> stopWords() {
        return new HashSet<>(Arrays.asList(
                "กล่าว",
                "กว่า",
                "กัน",
                "กับ",
                "การ",
                "ก็",
                "ก่อน",
                "ขณะ",
                "ขอ",
                "ของ",
                "ขึ้น",
                "คง",
                "ครั้ง",
                "ความ",
                "คือ",
                "จะ",
                "จัด",
                "จาก",
                "จึง",
                "ช่วง",
                "ซึ่ง",
                "ดัง",
                "ด้วย",
                "ด้าน",
                "ตั้ง",
                "ตั้งแต่",
                "ตาม",
                "ต่อ",
                "ต่าง",
                "ต่างๆ",
                "ต้อง",
                "ถึง",
                "ถูก",
                "ถ้า",
                "ทั้ง",
                "ทั้งนี้",
                "ทาง",
                "ที่",
                "ที่สุด",
                "ทุก",
                "ทํา",
                "ทําให้",
                "นอกจาก",
                "นัก",
                "นั้น",
                "นี้",
                "น่า",
                "นํา",
                "บาง",
                "ผล",
                "ผ่าน",
                "พบ",
                "พร้อม",
                "มา",
                "มาก",
                "มี",
                "ยัง",
                "รวม",
                "ระหว่าง",
                "รับ",
                "ราย",
                "ร่วม",
                "ลง",
                "วัน",
                "ว่า",
                "สุด",
                "ส่ง",
                "ส่วน",
                "สําหรับ",
                "หนึ่ง",
                "หรือ",
                "หลัง",
                "หลังจาก",
                "หลาย",
                "หาก",
                "อยาก",
                "อยู่",
                "อย่าง",
                "ออก",
                "อะไร",
                "อาจ",
                "อีก",
                "เขา",
                "เข้า",
                "เคย",
                "เฉพาะ",
                "เช่น",
                "เดียว",
                "เดียวกัน",
                "เนื่องจาก",
                "เปิด",
                "เปิดเผย",
                "เป็น",
                "เป็นการ",
                "เพราะ",
                "เพื่อ",
                "เมื่อ",
                "เรา",
                "เริ่ม",
                "เลย",
                "เห็น",
                "เอง",
                "แต่",
                "แบบ",
                "แรก",
                "และ",
                "แล้ว",
                "แห่ง",
                "โดย",
                "ใน",
                "ให้",
                "ได้",
                "ไป",
                "ไม่",
                "ไว้",
                "้ง"
        ));
    }
}
