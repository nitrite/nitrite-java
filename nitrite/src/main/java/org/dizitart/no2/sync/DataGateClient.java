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

package org.dizitart.no2.sync;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.dizitart.no2.sync.types.UserAgent;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import static org.dizitart.no2.sync.types.UserAgent.USER_AGENT;
import static org.dizitart.no2.util.StringUtils.isNullOrEmpty;

/**
 * Represents a DataGate server client for synchronous access.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
@Slf4j
public class DataGateClient {
    private String serverBaseUrl;
    private String username;
    private String password;
    private Proxy proxy;
    private boolean trustAllCerts;
    private long readTimeout = 0;
    private long connectTimeout = 0;
    private UserAgent userAgent;

    /**
     * Instantiates a new {@link DataGateClient}.
     *
     * @param serverBaseUrl the server base url
     */
    public DataGateClient(String serverBaseUrl) {
        this.serverBaseUrl = serverBaseUrl;
    }

    /**
     * Sets user credentials for basic authentication.
     *
     * @param username the username
     * @param password the password
     * @return the {@link DataGateClient}
     */
    public DataGateClient withAuth(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    /**
     * Sets the proxy details to connect to DataGate server.
     *
     * @param proxy the proxy
     * @return the {@link DataGateClient}
     */
    public DataGateClient withProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    /**
     * Trust all certificates.
     *
     * @return the {@link DataGateClient}
     */
    public DataGateClient trustAllCerts() {
        this.trustAllCerts = true;
        return this;
    }

    /**
     * Sets the read timeout for the underlying http client.
     *
     * @param milliseconds the timeout value in milliseconds
     * @return the {@link DataGateClient}
     */
    public DataGateClient readTimeout(long milliseconds) {
        this.readTimeout = milliseconds;
        return this;
    }

    /**
     * Sets the connection timeout for the underlying http client.
     *
     * @param milliseconds the timeout value in milliseconds
     * @return the {@link DataGateClient}
     */
    public DataGateClient connectTimeout(long milliseconds) {
        this.connectTimeout = milliseconds;
        return this;
    }

    /**
     * Sets the {@link UserAgent} details. The user agent details
     * helps to generate several analytics in DataGate server.
     *
     * @param userAgent the {@link UserAgent}
     * @return the {@link DataGateClient}
     */
    public DataGateClient withUserAgent(UserAgent userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /**
     * Gets the server base url.
     *
     * @return the remote url
     */
    protected String getServerBaseUrl() {
        return serverBaseUrl;
    }

    /**
     * Gets the underlying http client for the server-client communication.
     *
     * @return the http client
     */
    protected OkHttpClient getHttpClient() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

        if (!isNullOrEmpty(username)) {
            clientBuilder.addInterceptor(chain -> {
                Request request = chain.request();
                Request newRequest;
                newRequest = request.newBuilder()
                        .addHeader("Authorization", Credentials.basic(username, password))
                        .build();
                return chain.proceed(newRequest);
            });
        }

        if (userAgent != null) {
            clientBuilder.addInterceptor(chain -> {
                Request request = chain.request();
                Request newRequest;
                newRequest = request.newBuilder()
                        .addHeader(USER_AGENT, userAgent.toString())
                        .build();
                return chain.proceed(newRequest);
            });
        }

        if (proxy != null) {
            clientBuilder.proxy(proxy);
        }

        if (trustAllCerts) {
            try {
                // Create a trust manager that does not validate certificate chains
                final X509TrustManager[] trustManagers = new X509TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[0];
                        }
                    }
                };

                // Install the all-trusting trust manager
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustManagers, new java.security.SecureRandom());
                // Create an ssl socket factory with our all-trusting manager
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                // set ssl socket factory
                clientBuilder.sslSocketFactory(sslSocketFactory, trustManagers[0]);
            } catch (Exception e) {
                log.error("Error while bypassing certificate chains", e);
            }

            clientBuilder.hostnameVerifier((hostname, session) -> true);
        }

        if (readTimeout != 0) {
            clientBuilder.readTimeout(readTimeout, TimeUnit.MILLISECONDS);
        }

        if (connectTimeout != 0) {
            clientBuilder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
        }

        return clientBuilder.build();
    }
}
