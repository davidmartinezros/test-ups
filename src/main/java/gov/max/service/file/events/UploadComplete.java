package gov.max.service.file.events;

import gov.max.service.file.domain.model.Upload;
import org.springframework.context.ApplicationEvent;

import java.io.File;

public class UploadComplete extends ApplicationEvent {

    private Upload upload;

    public UploadComplete(Object source, Upload upload) {
        super(source);
        this.upload = upload;
    }

    public Upload getUpload() {
        return upload;
    }
}
