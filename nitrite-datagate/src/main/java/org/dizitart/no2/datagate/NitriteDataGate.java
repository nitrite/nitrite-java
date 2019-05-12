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

package org.dizitart.no2.datagate;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jongo.Jongo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Nitrite Data Gate Server application.
 *
 * @author Anindya Chatterjee."
 * @since 1.0
 */
@Slf4j
@EnableWebMvc
@Configuration
@EnableSwagger2
@EnableScheduling
@SpringBootApplication(exclude = { MongoAutoConfiguration.class, MongoDataAutoConfiguration.class })
public class NitriteDataGate extends WebMvcConfigurerAdapter {

    @Value("${datagate.mongo.host}")
    private String mongoHost;

    @Value("${datagate.mongo.port}")
    private int mongoPort;

    @Value("${datagate.mongo.user}")
    private String mongoUser;

    @Value("${datagate.mongo.password}")
    private String mongoPassword;

    @Value("${datagate.mongo.database}")
    private String mongoDatabase;

    public static void main(String[] args) {
        validateConfig(args);
        SpringApplication.run(NitriteDataGate.class, args);
    }

    private static void validateConfig(String[] args) {
        String runMode = System.getProperty("run.mode");
        if ("docker".equalsIgnoreCase(runMode)) {
            validateEnvConfig();
        } else {
            validatePropConfig(args);
        }
    }

    private static void validateEnvConfig() {
        String mongoHost = System.getenv("DATAGATE_MONGO_HOST");
        if (StringUtils.isEmpty(mongoHost)) {
            log.error("Environment variable DATAGATE_MONGO_HOST is not configured");
            System.exit(1);
        }

        String mongoPort = System.getenv("DATAGATE_MONGO_PORT");
        if (StringUtils.isEmpty(mongoPort)) {
            log.error("Environment variable DATAGATE_MONGO_PORT is not configured");
            System.exit(1);
        }

        String mongoUser = System.getenv("DATAGATE_MONGO_USER");
        if (StringUtils.isEmpty(mongoUser)) {
            log.error("Environment variable DATAGATE_MONGO_USER is not configured");
            System.exit(1);
        }

        String mongoPassword = System.getenv("DATAGATE_MONGO_PASSWORD");
        if (StringUtils.isEmpty(mongoPassword)) {
            log.error("Environment variable DATAGATE_MONGO_PASSWORD is not configured");
            System.exit(1);
        }

        String mongodb = System.getenv("DATAGATE_MONGO_DATABASE");
        if (StringUtils.isEmpty(mongodb)) {
            log.error("Environment variable DATAGATE_MONGO_DATABASE is not configured");
            System.exit(1);
        }
    }

    private static void validatePropConfig(String[] args) {
        InputStream input = null;
        Properties prop = new Properties();
        try {
            String configPath = extractConfigPath(args);
            if (StringUtils.isEmpty(configPath)) {
                return;
            }

            input = new FileInputStream(configPath);
            prop.load(input);

            String mongoHost = prop.getProperty("datagate.mongo.host");
            if (StringUtils.isEmpty(mongoHost)) {
                log.error("Property datagate.mongo.host is not configured");
                System.exit(1);
            }

            String mongoPort = prop.getProperty("datagate.mongo.port");
            if (StringUtils.isEmpty(mongoPort)) {
                log.error("Property datagate.mongo.port is not configured");
                System.exit(1);
            }

            String mongoUser = prop.getProperty("datagate.mongo.user");
            if (StringUtils.isEmpty(mongoUser)) {
                log.error("Property datagate.mongo.user is not configured");
                System.exit(1);
            }

            String mongoPassword = prop.getProperty("datagate.mongo.password");
            if (StringUtils.isEmpty(mongoPassword)) {
                log.error("Property datagate.mongo.password is not configured");
                System.exit(1);
            }

            String mongodb = prop.getProperty("datagate.mongo.database");
            if (StringUtils.isEmpty(mongodb)) {
                log.error("Property datagate.mongo.database is not configured");
                System.exit(1);
            }

        } catch (FileNotFoundException ignored) {
        } catch (Exception e) {
            log.error("Error while loading configuration", e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    private static String extractConfigPath(String[] args) {
        if (args != null && args.length > 0) {
            String arg0 = args[0];
            if (arg0 != null && arg0.contains("--spring.config.location")) {
                String[] split = arg0.split("=");
                if (split.length == 2) {
                    return split[1].trim();
                }
            }
        }
        return null;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
            .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/assets/**")
            .addResourceLocations("classpath:/assets/");
    }

    @Bean
    public Jongo jongo() {
        MongoCredential credential =
            MongoCredential.createCredential(mongoUser, mongoDatabase,
                mongoPassword.toCharArray());
        ServerAddress serverAddress = new ServerAddress(mongoHost, mongoPort);
        MongoClient mongoClient = new MongoClient(serverAddress,
            new ArrayList<MongoCredential>() {{
                add(credential);
            }});

        DB db = mongoClient.getDB(mongoDatabase);
        return new Jongo(db);
    }
}
