package gov.max.service.file.service.upload.imp;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public abstract class ImporterControllerBase extends AbstractSecurityWebApplicationInitializer {

    public static final String UPLOAD_DIR = System.getProperty("java.io.tmpdir");
    public static final String FLOW_CHUNK_NUMBER = "flowChunkNumber";
    public static final String FLOW_CHUNK_SIZE = "flowChunkSize";
    public static final String FLOW_TOTAL_SIZE = "flowTotalSize";
    public static final String FLOW_IDENTIFIER = "flowIdentifier";
    public static final String FLOW_FILENAME = "flowFilename";
    public static final String FLOW_RELATIVE_PATH = "flowRelativePath";
    public static final String FLOW_FILE = "file";

    protected FlowInfo writeFlowInfo(int chunkSize, long totalSize, String identifier, String filename, String relativePath) {
        //Here we add a ".temp" to every upload file to indicate NON-FINISHED
        String filePath = new File(UPLOAD_DIR, filename + "." + identifier).getAbsolutePath() + ".temp";

        FlowInfoStorage storage = FlowInfoStorage.getInstance();

        FlowInfo info = storage.writeFlowInfoIfNone(chunkSize, totalSize, identifier, filename, relativePath, filePath);
        if (!info.isValid()) {
            storage.remove(info);
            throw new IllegalArgumentException("Invalid request params.");
        }
        return info;
    }

    protected String processStream(int flowChunkNumber, FlowInfo info, MultipartFile part) {
        StringBuilder result = new StringBuilder();
        try {
            try (RandomAccessFile raf = new RandomAccessFile(info.getFlowFilePath(), "rw")) {

                //Seek to position
                long pos = (long) (flowChunkNumber - 1) * info.getFlowChunkSize();
                raf.seek(pos);

                long contentLength = part.getSize();

                long bytesReadTotal = 0;
                try (InputStream is = part.getInputStream()) {
                    byte[] bytes = new byte[1024 * 100];
                    while (bytesReadTotal < contentLength) {
                        int r = is.read(bytes);
                        if (r < 0) {
                            break;
                        }
                        raf.write(bytes, 0, r);
                        bytesReadTotal += r;
                    }
                }

                //Mark as uploaded.
                info.addChunk(flowChunkNumber, pos, pos + bytesReadTotal);
                if (info.checkIfUploadComplete()) {
                    //Check if all chunks uploaded, and change filename
                    FlowInfoStorage.getInstance().remove(info);
                    result.append("All finished.\n");
                } else {
                    result
                        .append("Upload: chunkNumber:")
                        .append(flowChunkNumber)
                        .append(", chunkSize:")
                        .append(info.getFlowChunkSize())
                        .append(", seekPosition:")
                        .append(pos).append(", contentLength:")
                        .append(contentLength)
                        .append(", totalSize:")
                        .append(info.getFlowTotalSize())
                        .append(" .");
                }
            }
        } catch (IOException ex) {
//            StackTraceFactory.printStackTrace(result, ex);
        }
        return result.toString();
    }

    protected FlowInfo readFlowInfo(String identifier) {
        FlowInfoStorage storage = FlowInfoStorage.getInstance();
        FlowInfo info = storage.readFlowInfo(identifier);
        return info;
    }

    protected ResponseEntity<String> doUploadChunk(
            @RequestParam(FLOW_CHUNK_NUMBER) int flowChunkNumber,
            @RequestParam(FLOW_CHUNK_SIZE) int flowChunkSize,
            @RequestParam(FLOW_TOTAL_SIZE) long flowTotalSize,
            @RequestParam(FLOW_IDENTIFIER) String flowIdentifier,
            @RequestParam(FLOW_FILENAME) String flowFileName,
            @RequestParam(FLOW_RELATIVE_PATH) String flowRelativePath,
            @RequestParam(FLOW_FILE) MultipartFile file) {

        FlowInfo info = writeFlowInfo(flowChunkSize, flowTotalSize, flowIdentifier, flowFileName, flowRelativePath);
        return new ResponseEntity<>(processStream(flowChunkNumber, info, file), HttpStatus.OK);
    }

    protected ResponseEntity<String> doCheckCunk(
            @RequestParam(FLOW_CHUNK_NUMBER) int flowChunkNumber,
            @RequestParam(FLOW_IDENTIFIER) String flowIdentifier) {

        StringBuilder result = new StringBuilder();
        FlowInfo info = readFlowInfo(flowIdentifier);
        if (info != null && info.containsChunk(flowChunkNumber)) {
            result.append("Uploaded.");
            return new ResponseEntity<>(result.toString(), HttpStatus.OK);
        } else {
            result.append("The chunk ").append(flowChunkNumber).append(" of ").append(flowIdentifier).append(" not found.");
            return new ResponseEntity<>(result.toString(), HttpStatus.NOT_FOUND);
        }
    }
}
