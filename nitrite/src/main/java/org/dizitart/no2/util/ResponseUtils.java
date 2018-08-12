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

package org.dizitart.no2.util;

import lombok.experimental.UtilityClass;
import okhttp3.Response;

/**
 * The OkHttp Response utility class.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
@UtilityClass
public class ResponseUtils {

    /**
     * Creates string representation of an error response.
     *
     * @param response the response
     * @return the string representation.
     */
    public static String errorResponse(Response response) {
        if (response != null) {
            return response.protocol().toString().toUpperCase() + " "
                    + response.code() + " "
                    + response.message();
        }
        return null;
    }
}
