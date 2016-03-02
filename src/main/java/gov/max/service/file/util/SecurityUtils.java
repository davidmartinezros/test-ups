package gov.max.service.file.util;

import gov.max.service.file.security.AppUserDetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityUtils {

    private final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);

    public AppUserDetails getUserDetails() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        AppUserDetails springSecurityUser = null;
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof AppUserDetails) {
                springSecurityUser = (AppUserDetails) authentication.getPrincipal();
            }
        }
        logger.debug("AppUserDetails {}", springSecurityUser);
        return springSecurityUser;
    }
}
