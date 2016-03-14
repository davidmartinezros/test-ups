package gov.max.service.file.security;

import gov.max.service.file.config.Constants;

import org.javers.spring.auditable.AuthorProvider;

import org.springframework.stereotype.Component;

@Component
public class SpringAuthorProvider implements AuthorProvider {

    @Override
    public String provide() {
        String userName = SecurityUtils.getCurrentUserLogin();
        return (userName != null ? userName : Constants.ANONYMOUS_ACCOUNT);
    }
}
