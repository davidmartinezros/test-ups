package gov.max.service.file.config;

import gov.max.service.file.util.EncryptionUtil;
import gov.max.service.file.util.AesEncryptionUtil;
import gov.max.service.file.util.HttpResponseUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    public static final String APP_LOGNAME = "application";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public EncryptionUtil encryptionUtil() {
        return new AesEncryptionUtil();
    }

    @Bean
    public HttpResponseUtil httpResponseUtil() { return new HttpResponseUtil(); }

    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger(APP_LOGNAME);
    }
}
