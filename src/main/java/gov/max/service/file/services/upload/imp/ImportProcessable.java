package gov.max.service.file.services.upload.imp;

import java.io.InputStream;

@FunctionalInterface
public interface ImportProcessable {
    String doImport(InputStream inputStream);
}
