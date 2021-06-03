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

package org.dizitart.no2.store;

import org.dizitart.no2.exceptions.SecurityException;
import org.dizitart.no2.store.memory.InMemoryStore;
import org.junit.Test;

import static org.junit.Assert.assertThrows;

public class UserAuthenticationServiceTest {
    @Test
    public void testConstructor() {
        // TODO: This test is incomplete.
        //   Reason: Nothing to assert: the constructed class does not have observers (e.g. getters or public fields).
        //   Add observers (e.g. getters or public fields) to the class.
        //   See https://diff.blue/R002

        new UserAuthenticationService(null);
    }

    @Test
    public void testAuthenticate() {
        assertThrows(SecurityException.class,
            () -> (new UserAuthenticationService(new InMemoryStore())).authenticate("janedoe", "iloveyou", true));
    }

    @Test
    public void testAuthenticate2() {
        // TODO: This test is incomplete.
        //   Reason: No meaningful assertions found.
        //   To help Diffblue Cover to find assertions, please add getters to the
        //   class under test that return fields written by the method under test.
        //   See https://diff.blue/R004

        (new UserAuthenticationService(new InMemoryStore())).authenticate("", "iloveyou", true);
    }

    @Test
    public void testAuthenticate3() {
        // TODO: This test is incomplete.
        //   Reason: No meaningful assertions found.
        //   To help Diffblue Cover to find assertions, please add getters to the
        //   class under test that return fields written by the method under test.
        //   See https://diff.blue/R004

        (new UserAuthenticationService(new InMemoryStore())).authenticate("janedoe", "", true);
    }

    @Test
    public void testAuthenticate4() {
        // TODO: This test is incomplete.
        //   Reason: No meaningful assertions found.
        //   To help Diffblue Cover to find assertions, please add getters to the
        //   class under test that return fields written by the method under test.
        //   See https://diff.blue/R004

        (new UserAuthenticationService(new InMemoryStore())).authenticate("janedoe", "iloveyou", false);
    }

    @Test
    public void testAuthenticate5() {
        // TODO: This test is incomplete.
        //   Reason: No meaningful assertions found.
        //   To help Diffblue Cover to find assertions, please add getters to the
        //   class under test that return fields written by the method under test.
        //   See https://diff.blue/R004

        (new UserAuthenticationService(new InMemoryStore())).authenticate("", "iloveyou", false);
    }
}

