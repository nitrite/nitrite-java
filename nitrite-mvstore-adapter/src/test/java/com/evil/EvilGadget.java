/*
 * Copyright (c) 2019-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evil;

import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Stands in for a third-party gadget class on the classpath (outside the
 * {@code org.dizitart.no2} / {@code java} allowlist) for GHSA-9297-g93h-86gg.
 */
public class EvilGadget implements Serializable {
    private static final long serialVersionUID = 1L;
    public static boolean readObjectCalled = false;

    private void readObject(ObjectInputStream in) throws Exception {
        in.defaultReadObject();
        readObjectCalled = true;
    }
}
