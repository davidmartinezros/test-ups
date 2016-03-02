package gov.max.service.file.util;

public class FileItem {
    private String fileName;
    private String mimeType;
    private String base64Content;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getBase64Content() {
        return base64Content;
    }

    public void setBase64Content(String base64Content) {
        this.base64Content = base64Content;
    }

    @Override
    public String toString() {
        return "[fileName=" + this.fileName + ", mimeType=" + this.mimeType + ", base64Content=" + this.base64Content + "]";
    }
}
