package org.dizitart.no2.sync.data;

import lombok.Getter;
import lombok.Setter;

import static org.dizitart.no2.util.StringUtils.isNullOrEmpty;

/**
 * Represents the usr agent of the DataGate client.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
@Getter @Setter
public class UserAgent {
    /**
     * The constant USER_AGENT.
     */
    public static final String USER_AGENT = "userAgent";
    private static final String separator = ";";

    /**
     * The application name.
     *
     * @param appName the application name
     * @return the application name.
     * */
    private String appName;

    /**
     * The application version.
     *
     * @param appName the application version
     * @return the application version.
     * */
    private String appVersion;

    /**
     * The device name.
     *
     * @param device the device name
     * @return the device name.
     * */
    private String device;

    /**
     * Parse a user agent string.
     *
     * @param userAgentString the user agent string
     * @return the {@link UserAgent} object parsed from the string.
     */
    public static UserAgent parse(String userAgentString) {
        if (!isNullOrEmpty(userAgentString)) {
            String[] split = userAgentString.split(separator);
            UserAgent userAgent = new UserAgent();
            for (int i = 0; i < split.length; i++) {
                switch (i) {
                    case 0:
                        userAgent.setAppName(split[0].trim());
                        break;
                    case 1:
                        userAgent.setAppVersion(split[1].trim());
                        break;
                    case 2:
                        userAgent.setDevice(split[2].trim());
                        break;
                    default:
                        break;
                }
            }
            return userAgent;
        }
        return null;
    }

    @Override
    public String toString() {
        return appName + separator + appVersion
                + separator + device;
    }
}
