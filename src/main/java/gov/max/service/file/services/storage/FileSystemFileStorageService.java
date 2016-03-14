package gov.max.service.file.services.storage;

import gov.max.service.file.domain.model.SharedLink;
import gov.max.service.file.services.guid.GuidProvider;
import gov.max.service.file.util.FileUtil;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import java.io.*;

@Service
public class FileSystemFileStorageService implements FileStorageService {

    private
    @Value("${spring.sharedLinkService.base.path}")
    String fileBasePath;

    @Autowired
    private GuidProvider guidProvider;

    @Autowired
    private SharedLinkService sharedLinkService;

    @Autowired
    private FileUtil fileUtil;

    @Autowired
    private Logger log;

    @Override
    public String save(InputStream inputStream, String fileName, String destination) {
        String retval = null;
        try {
            retval = uploadFile(inputStream, fileName, destination);
        } catch (Exception e) {
            log.error("unable to save file: {} in path: {}. Exception: {}", fileName, destination, e.getMessage());
        }
        return retval;
    }

    @Override
    public InputStream load(String path) throws FileNotFoundException {
        File file = new File(path);
        if (!file.isFile()) {
            throw new FileNotFoundException("Resource Not Found");
        }
        FileInputStream input = null;
        BufferedOutputStream output = null;
        try {
            input = new FileInputStream(file);
        } catch (Throwable t) {
            log.error("unable to create file stream for {}", path);
        }
        return input;
    }

    @Override
    public void delete(String id) {
        SharedLink model = sharedLinkService.findSharedLink(id);
        File srcFile = new File(fileBasePath, model.getFilePath());
        if (!FileUtils.deleteQuietly(srcFile)) {
            log.error("unable to delete file: {}. Path: {}", model.getFileName(), model.getFilePath());
        }
    }

    private String uploadFile(InputStream inputStream, String fileName, String destination) throws ServletException {

        String storedFileId = null;

        // URL: $config.uploadUrl, Method: POST, Content-Type: multipart/form-data
        // Unlimited file upload, each item will be enumerated as file-1, file-2, etc.
        // [$config.uploadUrl]?destination=/public_html/image.jpg&file-1={..}&file-2={...}
        try {
            storedFileId = guidProvider.getGuid();
            File f = new File(destination, fileName);
            if (!fileUtil.write(inputStream, f)) {
                throw new Exception("write error");
            }
        } catch (FileUploadException e) {
            throw new ServletException("Cannot parse multipart request: DiskFileItemFactory.parseRequest", e);
        } catch (IOException e) {
            throw new ServletException("Cannot parse multipart request: item.getInputStream", e);
        } catch (Exception e) {
            throw new ServletException("Cannot write file", e);
        } finally {
            return storedFileId;
        }

    }
}
