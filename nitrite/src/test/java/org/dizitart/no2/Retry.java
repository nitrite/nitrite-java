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

package org.dizitart.no2;

import lombok.extern.slf4j.Slf4j;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Nitrite tests retry rule.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
@Slf4j
public class Retry implements TestRule {
    private int retryCount;

    public Retry(int retryCount) {
        this.retryCount = retryCount;
    }

    public Statement apply(Statement base, Description description) {
        return statement(base, description);
    }

    private Statement statement(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Throwable caughtThrowable = null;

                // implement retry logic here
                for (int i = 0; i < retryCount; i++) {
                    try {
                        base.evaluate();
                        return;
                    } catch (Throwable throwable) {
                        caughtThrowable = throwable;
                        log.error(description.getDisplayName() + ": run " + (i+1) + " failed ");
                    }
                }
                log.info(description.getDisplayName() + ": giving up after " + retryCount + " failures ");
                if (caughtThrowable != null) {
                    throw caughtThrowable;
                }
            }
        };
    }
}
