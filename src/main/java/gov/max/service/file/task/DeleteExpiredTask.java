package gov.max.service.file.task;

import gov.max.service.file.domain.model.SharedLink;
import gov.max.service.file.domain.repositories.SharedLinkRepository;
import gov.max.service.file.services.storage.FileStorageService;

import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class DeleteExpiredTask {

    private static final String MAX_AGE = "spring.share.files.maxage";
    private static final int ONE_HOUR = 3600 * 1000;

    @Autowired
    private Environment env;

    @Autowired
    private Logger logger;

    @Autowired
    private SharedLinkRepository sharedLinkRepository;

    @Autowired
    private FileStorageService storageService;

//    @Scheduled(fixedRate = ONE_HOUR)
    public void deleteExpired() {
        logger.info("Run delete expired task: max file age is: '" + env.getProperty(MAX_AGE) +"'");

        int maxAge = Integer.parseInt(env.getProperty(MAX_AGE).trim());
        Instant expired = Instant.now().minus(maxAge, ChronoUnit.HOURS);
        List<SharedLink> links = sharedLinkRepository.findByCreatedBefore(expired);

        for (SharedLink link: links) {
            // NOTE: this is a soft delete. Delete the files, keep the db record and mark as expired
            link.setExpired(true);
            sharedLinkRepository.save(link);

            storageService.delete(link.getId());
        }
    }
}
