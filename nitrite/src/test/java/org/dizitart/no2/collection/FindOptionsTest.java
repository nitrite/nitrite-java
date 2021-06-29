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

package org.dizitart.no2.collection;

import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.common.SortableFields;
import org.junit.Test;

import java.text.Collator;
import java.text.RuleBasedCollator;

import static org.junit.Assert.*;

public class FindOptionsTest {
    @Test
    public void testConstructor() {
        FindOptions actualFindOptions = new FindOptions();
        actualFindOptions.limit(1L);
        actualFindOptions.skip(1L);
        Collator collatorResult = actualFindOptions.collator();
        assertTrue(collatorResult instanceof RuleBasedCollator);
        assertEquals(1L, actualFindOptions.skip().longValue());
        assertEquals(1L, actualFindOptions.limit().longValue());
        assertNull(actualFindOptions.orderBy());
        assertEquals(0, collatorResult.getDecomposition());
        assertEquals(
            "='​'=‌=‍=‎=‏=\u0000 =\u0001 =\u0002 =\u0003 =\u0004=\u0005 =\u0006 =\u0007 =\b ='\t'='\u000b' =\u000e=\u000f ='\u0010' =\u0011 =\u0012 =\u0013=\u0014 =\u0015 =\u0016 =\u0017 =\u0018=\u0019 =\u001a =\u001b =\u001c =\u001d=\u001e =\u001f"
                + " == = = = = == = = = = == = = = = == = = = = == = = = = == =;' ';' ';'"
                + " ';' ';' ';' ';' ';' ';' ';' ';' ';' ';' ';'　';'﻿';'\r' ;'\t' ;'\n"
                + "';'\f';'\u000b';́;̀;̆;̂;̌;̊;̍;̈;̋;̃;̇;̄;̷;̧;̨;̣;̲;̅;̉;̎;̏;̐;̑;̒;̓;̔;̕;̖;̗;̘;̙;̚;̛;̜;̝;̞;̟;̠;̡;̢;̤;̥;̦;̩;̪;"
                + "̫;̬;̭;̮;̯;̰;̱;̳;̴;̵;̶;̸;̹;̺;̻;̼;̽;̾;̿;͂;̈́;ͅ;͠;͡;҃;҄;҅;҆;⃐;⃑;⃒;⃓;⃔;⃕;⃖;⃗;⃘;⃙;⃚;⃛;⃜;⃝;⃞;⃟;⃠;⃡,'-';­;‐;"
                + "‑;‒;–;—;―;−<'_'<¯<','<';'<':'<'!'<¡<'?'<¿<'/'<'.'<´<'`'<'^'<¨<'~'<·<¸<'''<'\"'<«<»<'('<')'<'['<']'<'{"
                + "'<'}'<§<¶<©<®<'@'<¤<฿<¢<₡<₢<'$'<₫<€<₣<₤<₥<₦<₧<£<₨<₪<₩<¥<'*'<'\\'<'&'<'#'<'%'<'+'<±<÷<×<'<'<'='<'>'<¬<"
                + "'|'<¦<°<µ<0<1<2<3<4<5<6<7<8<9<¼<½<¾<a,A<b,B<c,C<d,D<ð,Ð<e,E<f,F<g,G<h,H<i,I<j,J<k,K<l,L<m,M<n,N<o,O<p"
                + ",P<q,Q<r,R<s, S & SS,ß<t,T& TH, Þ &TH, þ <u,U<v,V<w,W<x,X<y,Y<z,Z&AE,Æ&AE,æ&OE,Œ&OE,œ",
            ((RuleBasedCollator) collatorResult).getRules());
        assertEquals(2, collatorResult.getStrength());
    }

    @Test
    public void testConstructor2() {
        new FindOptions();
    }

    @Test
    public void testOrderBy() {
        SortableFields orderByResult = FindOptions.orderBy("Field Name", SortOrder.Ascending).orderBy();
        assertEquals("[Field Name]", orderByResult.toString());
        assertEquals("Field Name", orderByResult.getEncodedName());
    }

    @Test
    public void testSkipBy() {
        assertEquals(1L, FindOptions.skipBy(1L).skip().longValue());
    }

    @Test
    public void testLimitBy() {
        assertEquals(1L, FindOptions.limitBy(1L).limit().longValue());
    }

    @Test
    public void testSkip() {
        FindOptions findOptions = new FindOptions();
        FindOptions actualSkipResult = findOptions.skip(1);
        assertSame(findOptions, actualSkipResult);
        assertEquals(1L, actualSkipResult.skip().longValue());
    }

    @Test
    public void testSkip2() {
        FindOptions findOptions = new FindOptions();
        FindOptions actualSkipResult = findOptions.skip((Integer) null);
        assertSame(findOptions, actualSkipResult);
        assertNull(actualSkipResult.skip());
    }

    @Test
    public void testLimit() {
        FindOptions findOptions = new FindOptions();
        FindOptions actualLimitResult = findOptions.limit(1);
        assertSame(findOptions, actualLimitResult);
        assertEquals(1L, actualLimitResult.limit().longValue());
    }

    @Test
    public void testLimit2() {
        FindOptions findOptions = new FindOptions();
        FindOptions actualLimitResult = findOptions.limit((Integer) null);
        assertSame(findOptions, actualLimitResult);
        assertNull(actualLimitResult.limit());
    }

    @Test
    public void testThenOrderBy() {
        FindOptions findOptions = new FindOptions();
        FindOptions actualThenOrderByResult = findOptions.thenOrderBy("Field Name", SortOrder.Ascending);
        assertSame(findOptions, actualThenOrderByResult);
        SortableFields orderByResult = actualThenOrderByResult.orderBy();
        assertEquals("[Field Name]", orderByResult.toString());
        assertEquals("Field Name", orderByResult.getEncodedName());
    }

    @Test
    public void testThenOrderBy2() {
        FindOptions orderByResult = FindOptions.orderBy("Field Name", SortOrder.Ascending);
        assertSame(orderByResult, orderByResult.thenOrderBy("Field Name", SortOrder.Ascending));
    }
}

