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

package org.dizitart.no2.datagate.security;

import org.dizitart.no2.sync.types.UserAccount;

/**
 * A context to store authenticated user details per request.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public class RequestContext {
    private static final ThreadLocal<UserAccount> threadLocal;
    private static final RequestContext handler;

    static {
        threadLocal = new ThreadLocal<>();
        handler = new RequestContext();
    }

    private RequestContext() {}

    public static RequestContext getInstance() {
        return handler;
    }

    public void set(UserAccount userAccount) {
        threadLocal.set(userAccount);
    }

    public UserAccount get() {
        return threadLocal.get();
    }

    public void reset() {
        threadLocal.remove();
    }
}
