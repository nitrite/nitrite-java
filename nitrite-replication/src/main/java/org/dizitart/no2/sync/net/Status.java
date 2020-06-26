/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
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

package org.dizitart.no2.sync.net;

/**
 * @author Anindya Chatterjee
 */
public class Status {
    public final static int CONNECTED = 1;
    public final static int CONNECTING = 0;
    public final static int RECONNECT = 2;
    public final static int DISCONNECTED = -1;

    class CODE {
        public final static int NORMAL_CLOSE = 1000;
        public final static int ABNORMAL_CLOSE = 1001;
    }

    class TIP {
        public final static String NORMAL_CLOSE = "normal close";
        public final static String ABNORMAL_CLOSE = "abnormal close";
    }
}
