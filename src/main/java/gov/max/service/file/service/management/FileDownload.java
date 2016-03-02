package gov.max.service.file.service.management;

import java.io.InputStream;

public class FileDownload {

    private InputStream stream;
    private String filePath;
    private String fileName;
    private String createdBy;
    private String fileType;
    private long fileSize;

    FileDownload(InputStream stream, String filePath, String fileName, String fileType, long fileSize, String createdBy) {
        this.stream = stream;
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }

    public InputStream getStream() {
        return stream;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public long getFileSize() {
        return fileSize;
    }
}
