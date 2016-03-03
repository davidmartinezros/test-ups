package gov.max.service.file.services.management;

public class FileInfo {

    private String fileName;
    private String filePath;
    private String publicId;
    private String password;
    private String createdBy;
    private Boolean deleted;
    private Boolean expired;
    private String sessionDir;
    private long fileSize;
    private boolean passwordProtected;

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSessionDir(String sessionDir) {
        this.sessionDir = sessionDir;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public FileInfo() {}

    public FileInfo(String fileName, String filePath, String createdBy, long fileSize, boolean passwordProtected, Boolean deleted, Boolean expired) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.createdBy = createdBy;
        this.fileSize = fileSize;
        this.deleted = deleted;
        this.expired = expired;
        this.setPasswordProtected(passwordProtected);
    }

    public FileInfo(String fileName, String filePath, String publicId, String password, String createdBy, long fileSize, boolean passwordProtected) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.publicId = publicId;
        this.password = password;
        this.createdBy = createdBy;
        this.fileSize = fileSize;
        this.setPasswordProtected(passwordProtected);
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getPublicId() {
        return publicId;
    }

    public String getPassword() {
        return password;
    }

    public String getSessionDir() {
        return sessionDir;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public Boolean getExpired() {
        return expired;
    }

    public long getFileSize() {
        return fileSize;
    }

    public boolean isPasswordProtected() {
        return passwordProtected;
    }

    public void setPasswordProtected(boolean passwordProtected) {
        this.passwordProtected = passwordProtected;
    }
}
