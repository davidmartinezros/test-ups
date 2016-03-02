package gov.max.service.file.web.model;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

public class UploadFormModel {

    @NotNull
    private MultipartFile file;

    private String password;
    private String destination;
    private int flowChunkNumber;
    private int flowChunkSize;
    private int flowCurrentChunkSize;
    private String flowFilename;
    private String flowIdentifier;
    private String flowRelativePath;
    private int flowTotalChunks;
    private long flowTotalSize;

    public int getFlowChunkNumber() {
        return flowChunkNumber;
    }

    public void setFlowChunkNumber(int flowChunkNumber) {
        this.flowChunkNumber = flowChunkNumber;
    }

    public int getFlowChunkSize() {
        return flowChunkSize;
    }

    public void setFlowChunkSize(int flowChunkSize) {
        this.flowChunkSize = flowChunkSize;
    }

    public int getFlowCurrentChunkSize() {
        return flowCurrentChunkSize;
    }

    public void setFlowCurrentChunkSize(int flowCurrentChunkSize) {
        this.flowCurrentChunkSize = flowCurrentChunkSize;
    }

    public String getFlowFilename() {
        return flowFilename;
    }

    public void setFlowFilename(String flowFilename) {
        this.flowFilename = flowFilename;
    }

    public String getFlowIdentifier() {
        return flowIdentifier;
    }

    public void setFlowIdentifier(String flowIdentifier) {
        this.flowIdentifier = flowIdentifier;
    }

    public String getFlowRelativePath() {
        return flowRelativePath;
    }

    public void setFlowRelativePath(String flowRelativePath) {
        this.flowRelativePath = flowRelativePath;
    }

    public int getFlowTotalChunks() {
        return flowTotalChunks;
    }

    public void setFlowTotalChunks(int flowTotalChunks) {
        this.flowTotalChunks = flowTotalChunks;
    }

    public long getFlowTotalSize() {
        return flowTotalSize;
    }

    public void setFlowTotalSize(long flowTotalSize) {
        this.flowTotalSize = flowTotalSize;
    }

    public MultipartFile getFile() {
        return this.file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDestination() {
        return this.destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
