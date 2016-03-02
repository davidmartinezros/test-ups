package gov.max.service.file.service.storage;

import java.io.FileNotFoundException;
import java.io.InputStream;

public interface FileStorageService {

    String save(InputStream inputStream, String fileName, String destination);
    InputStream load(String path) throws FileNotFoundException;
    void delete(String id);

}
