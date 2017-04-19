package org.dizitart.no2.datagate.security;

import org.dizitart.no2.sync.data.UserAccount;

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
