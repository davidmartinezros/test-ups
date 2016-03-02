package gov.max.service.file.config;

import gov.max.service.file.service.upload.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Create the UploadService and hard code where it will store the uploaded
 * files.
 */
@Configuration
public class    UploadServiceConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(UploadServiceConfiguration.class);

    @Bean(name = { "uploadService" })
    public UploadService localStorageFileUploadServiceImpl() {
        String baseLocation = "src/main/webapp/assets/uploads";

        LOG.info("Upload Service Storage Location: \"{}\"", baseLocation);

        UploadService rtn = new UploadService()
                .withBaseLocation(baseLocation);

        return rtn;
    }
}
