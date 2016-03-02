package gov.max.service.file.domain.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.max.service.file.domain.util.CustomLocalDateSerializer;
import gov.max.service.file.domain.util.ISO8601LocalDateDeserializer;

import org.joda.time.LocalDate;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A Upload.
 */
@Document(collection = "UPLOAD")
public class Upload implements Serializable {

    private static final long serialVersionUID = -8623921569188303707L;

    @Id
    private String id;

    @Field("parent_id")
    private String parentId;

    @Field("original_name")
    private String originalName;

    @Field("file_type")
    private String fileType;

    @Field("file_path")
    private String filePath;

    @Field("file_directory_path")
    private String fileDirectoryPath;

    @Field("chunk_directory")
    private String chunkDirectory;

    /*
     * Should really be a LocalDateTime.
     */
    @JsonSerialize(using = CustomLocalDateSerializer.class)
    @JsonDeserialize(using = ISO8601LocalDateDeserializer.class)
    @Field("uploaded_at")
    private LocalDate uploadedAt;

    @Field("md5sum")
    private String md5sum;

    @Field("upload_complete")
    private Boolean uploadComplete;

    @Field("total_chunks")
    private Integer totalChunks;

    @Field("total_size")
    private Long totalSize;

    /*
     * Should really be a LocalDateTime.
     */
    @JsonSerialize(using = CustomLocalDateSerializer.class)
    @JsonDeserialize(using = ISO8601LocalDateDeserializer.class)
    @Field("completed_at")
    private LocalDate completedAt;

    /*
     * Manually added because JHipster doesn't generate this type.
     */
    private List<Boolean> chunks;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public LocalDate getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDate uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getMd5sum() {
        return md5sum;
    }

    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }

    public Boolean getUploadComplete() {
        return uploadComplete;
    }

    public void setUploadComplete(Boolean uploadComplete) {
        this.uploadComplete = uploadComplete;
    }

    public Integer getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(Integer totalChunks) {
        this.totalChunks = totalChunks;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public LocalDate getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDate completedAt) {
        this.completedAt = completedAt;
    }

    public List<Boolean> getChunks() {
        return chunks;
    }

    public void setChunks(List<Boolean> chunks) {
        this.chunks = chunks;
    }

    public String getChunkDirectory() {
        return chunkDirectory;
    }

    public void setChunkDirectory(String chunkDirectory) {
        this.chunkDirectory = chunkDirectory;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileDirectoryPath() {
        return fileDirectoryPath;
    }

    public void setFileDirectoryPath(String fileDirectoryPath) {
        this.fileDirectoryPath = fileDirectoryPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Upload upload = (Upload) o;

        if (!Objects.equals(id, upload.id))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Upload{" + "id=" + id + ", parentId='" + parentId + ", originalName='" + originalName + "'"
                + ", uploadedAt='" + uploadedAt + "'" + ", md5sum='" + md5sum
                + "'" + ", uploadComplete='" + uploadComplete + "'"
                + ", totalChunks='" + totalChunks + "'" + ", totalSize='"
                + totalSize + "'" + ", completedAt='" + completedAt + "'" + '}';
    }
}
