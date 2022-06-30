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

import org.dizitart.no2.store.memory.InMemoryStore;
import org.junit.Test;

public class UserAuthenticationServiceTest {
    @Test
    public void testConstructor() {
        new UserAuthenticationService(null);
    }

    @Test
    public void testAuthenticate2() {
        (new UserAuthenticationService(new InMemoryStore())).authenticate("", "iloveyou");
    }

    @Test
    public void testAuthenticate3() {
        (new UserAuthenticationService(new InMemoryStore())).authenticate("janedoe", "");
    }

    @Test
    public void testAuthenticate4() {
        (new UserAuthenticationService(new InMemoryStore())).authenticate("janedoe", "iloveyou");
    }

    @Test
    public void testAuthenticate5() {
        (new UserAuthenticationService(new InMemoryStore())).authenticate("", "iloveyou");
    }
}

