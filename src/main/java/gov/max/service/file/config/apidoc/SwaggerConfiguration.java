package gov.max.service.file.config.apidoc;

import gov.max.service.file.config.Constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;

import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Date;

import static springfox.documentation.builders.PathSelectors.regex;

/**
 * Springfox Swagger configuration.
 *
 * Warning! When having a lot of REST endpoints, Springfox can become a performance issue. In that
 * case, you can use a specific Spring profile for this class, so that only front-end developers
 * have access to the Swagger view.
 */
@Configuration
@EnableSwagger2
@Profile("!" + Constants.SPRING_PROFILE_PRODUCTION)
public class SwaggerConfiguration {

    private final Logger log = LoggerFactory.getLogger(SwaggerConfiguration.class);

    public static final String DEFAULT_INCLUDE_PATTERN = "/api/.*";

    @Autowired
    private Environment env;

    /**
     * Swagger Springfox configuration.
     */
    @Bean
//    @Profile("!" + Constants.SPRING_PROFILE_FAST)
    public Docket swaggerSpringfoxDocket() {
        log.debug("Starting Swagger");
        StopWatch watch = new StopWatch();
        watch.start();
        ApiInfo apiInfo = new ApiInfo(
            env.getProperty("max-drive.swagger.title"),
            env.getProperty("max-drive.swagger.description"),
            env.getProperty("max-drive.swagger.version"),
            env.getProperty("max-drive.swagger.termsOfServiceUrl"),
            env.getProperty("max-drive.swagger.contact"),
            env.getProperty("max-drive.swagger.license"),
            env.getProperty("max-drive.swagger.licenseUrl"));

        Docket docket = new Docket(DocumentationType.SWAGGER_2)
            .apiInfo(apiInfo)
            .genericModelSubstitutes(ResponseEntity.class)
            .forCodeGeneration(true)
            .genericModelSubstitutes(ResponseEntity.class)
            .ignoredParameterTypes(Pageable.class)
            .directModelSubstitute(java.time.LocalDate.class, String.class)
            .directModelSubstitute(java.time.ZonedDateTime.class, Date.class)
            .directModelSubstitute(java.time.LocalDateTime.class, Date.class)
            .select()
            .paths(regex(DEFAULT_INCLUDE_PATTERN))
            .build();
        watch.stop();
        log.debug("Started Swagger in {} ms", watch.getTotalTimeMillis());
        return docket;
    }
}
