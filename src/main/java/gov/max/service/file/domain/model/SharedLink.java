package gov.max.service.file.domain.model;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

@Document(collection = "SHARED_LINK")
public class SharedLink implements Serializable { //extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = -8623215639438303707L;

    @org.springframework.data.annotation.Id
    private String id;

    @Field("public_id")
    private String publicId;

    @Field("storage_id")
    private String storageId;

    @Field("deleted")
    private Boolean deleted = false;

    @Field
    private Boolean expired = false;

    @Field
    private Date expiration;

    @Field
    private Date created;

    @Field
    private String fileName;

    @Field
    private String createdBy;

    @Field
    private String fileType;

    @Field
    private String filePath;

    @Field
    private long fileSize;

    @Field
    private String password;

    @Field
    private byte[] encryptionKey;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        if (deleted != null) {
            this.deleted = deleted;
        } else {
            this.deleted = false;
        }
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        if (expired != null) {
            this.expired = expired;
        } else {
            this.expired = false;
        }
    }

    public Date getExpirationDate() {
        return expiration;
    }

    public void setExpirationDate(Date date) {
        this.expiration = date;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Integer expiration) {

        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, expiration); // add expiration

        this.expiration = cal.getTime();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public byte[] getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(byte[] encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

}
