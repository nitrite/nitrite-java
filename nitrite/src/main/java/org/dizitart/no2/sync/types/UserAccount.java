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

package org.dizitart.no2.sync.types;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the user account in DataGate server.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
@Data
public class UserAccount {

    /**
     * The user name.
     *
     * @param userName the user name
     * @return the user name.
     * */
    private String userName;

    /**
     * The user password.
     *
     * @param password the user password
     * @return the user password.
     * */
    private String password;

    /**
     * A boolean value indicating whether account has
     * been expired or not.
     *
     * @param accountNonExpired `false` if expired; `true` otherwise.
     * */
    @Getter(AccessLevel.NONE)
    private boolean accountNonExpired = true;

    /**
     * A boolean value indicating whether account has
     * been locked or not.
     *
     * @param accountNonLocked `false` if locked; `true` otherwise.
     * */
    @Getter(AccessLevel.NONE)
    private boolean accountNonLocked = true;

    /**
     * A boolean value indicating whether account has
     * been enabled or not.
     *
     * @param enabled `true` if enabled; `false` otherwise.
     * */
    @Getter(AccessLevel.NONE)
    private boolean enabled = true;

    /**
     * The list of authorities associated with the user.
     *
     * @param authorities the list of authorities
     * @return the list of authorities.
     * */
    private String[] authorities;

    /**
     * The list of collection permitted to be accessed by the user.
     *
     * @param collections the list of collections
     * @return the list of collections.
     * */
    private List<String> collections = new ArrayList<>();

    /**
     * Gets a boolean value indicating if account has been expired
     * or not.
     *
     * @return `false` if expired; `true` otherwise.
     */
    public boolean getAccountNonExpired() {
        return accountNonExpired;
    }

    /**
     * Gets a boolean value indicating if account has been locked
     * or not.
     *
     * @return `false` if locked; `true` otherwise.
     */
    public boolean getAccountNonLocked() {
        return accountNonLocked;
    }

    /**
     * Gets a boolean value indicating if account is enabled
     * or not.
     *
     * @return `true` if enabled; `false` otherwise.
     */
    public boolean getEnabled() {
        return enabled;
    }
}
