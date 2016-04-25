package gov.max.service.file.web.rest;

import java.util.*;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ActiveProfilesResource {

    @Inject
    Environment env;

    @RequestMapping("/activeProfiles")
    public ActiveProfileResponse getActiveProfiles() {
        return new ActiveProfileResponse(env.getActiveProfiles(), getRibbonEnv());
    }

    private String getRibbonEnv() {

        String[] activeProfiles = env.getActiveProfiles();
        List<String> ribbonProfiles = null;
        String diplayActiveProfiles = env.getProperty("ribbon.displayOnActiveProfiles");
        if (diplayActiveProfiles != null) {
            ribbonProfiles = Arrays.asList(diplayActiveProfiles.split(","));
        }
        List<String> springBootProfiles = Arrays.asList(activeProfiles);
        Set<String> uniqueProfiles = new HashSet<String>(springBootProfiles);
        uniqueProfiles.retainAll(ribbonProfiles);

        if (uniqueProfiles.size() > 0) {
            return uniqueProfiles.iterator().next();
        }
        return null;
    }

    class ActiveProfileResponse {

        public String[] activeProfiles;
        public String ribbonEnv;

        ActiveProfileResponse(String[] activeProfiles, String ribbonEnv) {
            this.activeProfiles = activeProfiles;
            this.ribbonEnv = ribbonEnv;
        }

    }

}