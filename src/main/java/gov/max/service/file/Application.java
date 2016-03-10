package gov.max.service.file;

import gov.max.service.file.config.Constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.MultipartConfigElement;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;

@Configuration
@SpringBootApplication
@EnableScheduling
public class Application extends SpringBootServletInitializer {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Inject
    private Environment env;

    private
    @Value("${spring.multipart.location}")
    String multipartLocation;

    private
    @Value("${spring.multipart.maxFileSize}")
    String maxFileSize;

    private
    @Value("${spring.multipart.maxRequestSize}")
    String maxRequestSize;

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    public static void main(String[] args) throws UnknownHostException {
        SpringApplication app = new SpringApplication(Application.class);
        SimpleCommandLinePropertySource source = new SimpleCommandLinePropertySource(args);
//        addDefaultProfile(app, source);
        Environment env = app.run(args).getEnvironment();
        log.info(
            "Access URLs:" +
            "\n----------------------------------------------------------\n\t"
            + "Local: \t\thttp://127.0.0.1:{}\n\t"
            + "External: \thttp://{}:{}" +
            "\n----------------------------------------------------------",
            env.getProperty("server.port"),
            InetAddress.getLocalHost().getHostAddress(),
            env.getProperty("server.port")
        );
    }

    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(maxFileSize);
        factory.setMaxRequestSize(maxRequestSize);
        factory.setLocation(multipartLocation);
        return factory.createMultipartConfig();
    }

    /**
     * Initializes upload_example.
     * <p/>
     * Spring profiles can be configured with a program arguments
     * --spring.profiles.active=your-active-profile
     * <p/>
     */
    @PostConstruct
    public void initApplication() throws IOException {
        if (env.getActiveProfiles().length == 0) {
            log.warn("No Spring profile configured, running with default configuration");
        } else {
            log.info("Running with Spring profile(s) : {}", Arrays.toString(env.getActiveProfiles()));
            Collection activeProfiles = Arrays.asList(env.getActiveProfiles());
            if (activeProfiles.contains("dev") && activeProfiles.contains("prod")) {
                log.error("You have misconfigured your application! "
                        + "It should not run with both the 'dev' and 'prod' profiles at the same time.");
            }
            if (activeProfiles.contains("prod") && activeProfiles.contains("test")) {
                log.error("You have misconfigured your application! "
                        + "It should not run with both the 'prod' and 'test' profiles at the same time.");
            }
            if (activeProfiles.contains("dev") && activeProfiles.contains("test")) {
                log.error("You have misconfigured your application! "
                        + "It should not run with both the 'dev' and 'test' profiles at the same time.");
            }
        }
    }

    /**
     * If no profile has been configured, set by default the "dev" profile.
     */
    private static void addDefaultProfile(SpringApplication app, SimpleCommandLinePropertySource source) {
        if (!source.containsProperty("spring.profiles.active") &&
                !System.getenv().containsKey("SPRING_PROFILES_ACTIVE")) {
            app.setAdditionalProfiles(Constants.SPRING_PROFILE_DEVELOPMENT, Constants.SPRING_PROFILE_NATIVE);
        }
    }
}
