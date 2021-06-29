/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.common;

import org.dizitart.no2.common.tuples.Pair;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class FieldValuesTest {
    @Test
    public void testConstructor() {
        assertEquals("FieldValues(nitriteId=null, fields=[], values=[])", (new FieldValues()).toString());
    }

    @Test
    public void testGet() {
        FieldValues fieldValues = new FieldValues();
        fieldValues.setFields(new Fields());
        assertNull(fieldValues.get("Field"));
    }

    @Test
    public void testGet2() {
        Fields fields = new Fields();
        fields.addField("Field");

        FieldValues fieldValues = new FieldValues();
        fieldValues.setFields(fields);
        assertNull(fieldValues.get("Field"));
    }

    @Test
    public void testGetFields() {
        FieldValues fieldValues = new FieldValues();
        assertEquals("[]", fieldValues.getFields().toString());
        assertEquals("FieldValues(nitriteId=null, fields=[], values=[])", fieldValues.toString());
    }

    @Test
    public void testGetFields2() {
        FieldValues fieldValues = new FieldValues();
        Fields fields = new Fields();
        fieldValues.setFields(fields);
        assertSame(fields, fieldValues.getFields());
    }

    @Test
    public void testGetFields3() {
        ArrayList<Pair<String, Object>> pairList = new ArrayList<>();
        pairList.add(new Pair<>());

        FieldValues fieldValues = new FieldValues();
        fieldValues.setValues(pairList);
        fieldValues.getFields();
        assertEquals("FieldValues(nitriteId=null, fields=[], values=[Pair(first=null, second=null)])",
            fieldValues.toString());
    }

    @Test
    public void testGetFields4() {
        ArrayList<Pair<String, Object>> pairList = new ArrayList<>();
        pairList.add(Pair.pair("First", "Second"));

        FieldValues fieldValues = new FieldValues();
        fieldValues.setValues(pairList);
        fieldValues.getFields();
        assertEquals("FieldValues(nitriteId=null, fields=[First], values=[Pair(first=First, second=Second)])",
            fieldValues.toString());
    }
}

