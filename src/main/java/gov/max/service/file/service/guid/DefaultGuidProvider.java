package gov.max.service.file.service.guid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Default GUID provider implementation.
 */
@Service
public class DefaultGuidProvider implements GuidProvider {

    private static final Logger logger = LoggerFactory.getLogger(DefaultGuidProvider.class);

    @Override
    public String getGuid() throws GuidException {
        String guid = UUID.randomUUID().toString();

        if (logger.isTraceEnabled())
            logger.trace("Generated GUID: {}", guid);

        return guid;
    }

}