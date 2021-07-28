/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.sync.net;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocketListener;
import org.dizitart.no2.sync.Config;
import org.dizitart.no2.sync.ReplicationException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.util.concurrent.TimeUnit;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class DataGateSocket {
    private final Config config;

    private OkHttpClient httpClient;
    private Request request;

    public DataGateSocket(Config config) {
        this.config = config;
        configure(config);
    }

    public void setListener(WebSocketListener listener) {
        try {
            createWebSocket(listener);
        } catch (Exception e) {
            log.error("Failed to connect to remote datagate server", e);
            throw new ReplicationException("remote datagate connection failed", e, true);
        }
    }

    private void configure(Config config) {
        this.httpClient = createClient();
        this.request = config.getRequestBuilder().build();
    }

    private OkHttpClient createClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(config.getTimeout().getTime(),
                config.getTimeout().getTimeUnit())
            .readTimeout(config.getTimeout().getTime(),
                config.getTimeout().getTimeUnit())
            .writeTimeout(config.getTimeout().getTime(),
                config.getTimeout().getTimeUnit())
            .pingInterval(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false);

        if (config.getProxy() != null) {
            builder.proxy(config.getProxy());
        }

        if (config.isAcceptAllCertificates()) {
            try {
                final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {

                        @Override
                        @SuppressWarnings("TrustAllX509TrustManager")
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) {
                        }

                        @Override
                        @SuppressWarnings("TrustAllX509TrustManager")
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
                };

                // SSLContext needs to be compatible with TLS 1.2
                final SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);

                builder.hostnameVerifier((hostname, session) -> true);
            } catch (Exception e) {
                throw new ReplicationException("error while configuring SSLSocketFactory", e, true);
            }
        }

        return builder.build();
    }

    private void createWebSocket(WebSocketListener webSocketListener) {
        if (httpClient != null) {
            httpClient.dispatcher().cancelAll();
            httpClient.newWebSocket(request, webSocketListener);
        }
    }
}
