package gov.max.service.file.events;

import gov.max.service.file.domain.model.Upload;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

@Component
public class UploadCompleteEventPublisher implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher publisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    public void publishCompletedUpload(Upload upload) {
        publisher.publishEvent(new UploadComplete(this, upload));
    }
}
