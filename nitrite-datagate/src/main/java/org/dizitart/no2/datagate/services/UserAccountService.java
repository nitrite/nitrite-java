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

package org.dizitart.no2.datagate.services;

import com.mongodb.WriteResult;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.sync.types.UserAccount;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.dizitart.no2.datagate.Constants.USER_REPO;

/**
 * A user account service used for authentication.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Slf4j
@Service
public class UserAccountService {
    private MongoCollection userRepository;

    @Autowired
    public UserAccountService(Jongo jongo) {
        this.userRepository = jongo.getCollection(USER_REPO);
        this.userRepository.ensureIndex("{ userName: 1 }", "{ unique: true }");
    }

    public List<UserAccount> findUsersByAuthorities(String... authorities) {
        MongoCursor<UserAccount> userAccounts = userRepository.find("{authorities: { $in: [#]}}",
            String.join(",", authorities))
            .as(UserAccount.class);
        return StreamSupport
            .stream(userAccounts.spliterator(), false)
            .collect(Collectors.toList());
    }

    public UserAccount findByUsername(String username) {
        UserAccount userAccount = userRepository
                .findOne("{userName: '" + username + "'}")
                .as(UserAccount.class);
        if (userAccount != null) {
            log.debug("A user found with username " + username);
        } else {
            log.debug("No user found with username " + username);
        }
        return userAccount;
    }

    public void insert(UserAccount userAccount) {
        userRepository.save(userAccount);
    }

    public void delete(String username) {
        WriteResult remove = this.userRepository.remove("{userName: '" + username + "'}");
        log.info("Removed " + remove.toString());
    }

    public boolean isNoUserFound() {
        return userRepository.count() == 0;
    }

    public void update(UserAccount userAccount) {
        if (userAccount != null) {
            userRepository.update("{ userName: # }", userAccount.getUserName())
                .with(userAccount);
        }
    }
}
