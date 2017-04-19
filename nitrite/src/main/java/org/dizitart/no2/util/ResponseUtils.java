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
