package org.dizitart.no2.datagate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Swagger config.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket swaggerSettings() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.dizitart.no2.datagate"))
                .paths(PathSelectors.ant("/api/**"))
                .build();
    }
}
