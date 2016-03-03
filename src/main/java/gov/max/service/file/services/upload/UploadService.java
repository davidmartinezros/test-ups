package gov.max.service.file.services.upload;

import com.codahale.metrics.annotation.Timed;

import gov.max.service.file.domain.model.Upload;
import gov.max.service.file.domain.repositories.UploadRepository;
import gov.max.service.file.events.UploadCompleteEventPublisher;
import gov.max.service.file.services.storage.SharedLinkService;
import gov.max.service.file.util.FileUtil;
import gov.max.service.file.util.SecurityUtils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileExistsException;

import org.joda.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * An implementation of the UploadService for flow.js.
 *
 * This version will just store the chunks and the file locally. Nothing else is
 * done in terms of tracking who uploaded it or making it available for
 * download.
 */
@Service
public class UploadService implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(UploadService.class);

    @Inject
    private UploadRepository uploadRepository;

    @Autowired
    SecurityUtils securityUtils;

    @Autowired
    SharedLinkService sharedLinkService;

    private
    @Value("${spring.repository.base.path}")
    String fileBasePath;

    @Autowired
    private FileUtil fileUtil;

    @Autowired
    private UploadCompleteEventPublisher uploadCompleteEventPublisher;

    private String baseLocation = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        /**
         * Our only property must be set.
         */
        if (baseLocation == null) {
            return;
        }

        /**
         * Create the base location and all parent paths.
         */
        fileUtil.createBaseLocation();
    }

    /**
     * Get the base location for the file storage.
     *
     * @return the base location
     */
    public String getBaseLocation() {
        return baseLocation;
    }

    /**
     * Set the base location for the file storage.
     *
     * @param baseLocation
     *            the base location
     *
     * @return this
     */
    public UploadService withBaseLocation(String baseLocation) {
        this.baseLocation = baseLocation;
        return this;
    }

    public Upload getUpload(String flowIdentifier) {
        UUID uuid = UUID.fromString(flowIdentifier);
        return uploadRepository.findOne(uuid.toString());
    }

    public Upload saveChunk(String flowIdentifier, int flowChunkNumber,
            int flowChunkSize, int flowCurrentChunkSize, String flowFilename,
            int flowTotalChunks, long flowTotalSize, MultipartFile file)
            throws IOException {
        /*
         * Get the Upload to see if we have already started this upload. We will
         * attach the next chuck if there is one or create a new entry
         * otherwise.
         */
        Upload upload = this.getUpload(flowIdentifier);

        /*
         * Force the creation of the upload record to be handled in the
         * testChunk function.
         */
        if (upload == null) {
            throw new IOException("Upload record does not exist.");
        }

        /*
         * Test that the file hasn't changed since the last chunk test.
         */
        testFlowUnchanged(upload, flowFilename, flowTotalChunks, flowTotalSize);

        /*
         * Check if the chunk has already been uploaded. It seems that this is
         * normal case when you pause/resume. So just return the Upload as it
         * currently stands.
         */
        if (upload.getChunks().get(flowChunkNumber - 1)) {
            LOG.warn("chunk already uploaded: {}", flowChunkNumber - 1);
            return upload;
        }

        /*
         * Add this chunk to the chunk directory and set the chunk list element
         * to true.
         */
        try {
            upload = fileUtil.saveChunkToDisk(upload, flowChunkNumber, file);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }

        /*
         * Check to see if the upload is complete. If all the chunks are in then
         * it is and we should mark it as such and trigger publish event to the
         * system.
         */
        if (uploadComplete(upload)) {
            LOG.trace("flow complete");
            upload.setUploadComplete(true);
            upload = uploadRepository.save(upload);

            uploadCompleteEventPublisher.publishCompletedUpload(upload);
        }

        /*
         * If the upload is complete, then go ahead and process the file.
         */
        if (upload.getUploadComplete() == true) {
            /*
             * Concatenate all the chunks.
             */
            upload = mergeChunks(upload);

            /*
             * Calculate and set the MD5SUM.
             */
            upload.setMd5sum(calculateMd5Sum(flowIdentifier));

            /*
             * rename file to original name
             */
            this.getFile(upload.getId()).renameTo(new File(upload.getFileDirectoryPath() +
                        File.separator +
                        upload.getOriginalName()));

            /*
             * Record the completed time.
             */
            upload.setCompletedAt(new LocalDate());
            upload = uploadRepository.save(upload);

            LOG.trace("upload: {}", upload);
        }

        LOG.debug("upload: {}", upload);

        return upload;
    }

    public boolean testChunk(String flowIdentifier, int flowChunkNumber,
            int flowChunkSize, int flowCurrentChunkSize, String flowFilename,
            int flowTotalChunks, long flowTotalSize) throws IOException {

        /*
         * Confirm that the flowIdentifier is an UUID and use it from here on
         * out.
         */
        UUID uuid = UUID.fromString(flowIdentifier);

        /*
         * Get the current UUID to see if we have already started this upload.
         * Will attach the next chuck if there is one or create a new entry
         * otherwise.
         */
        Upload upload = uploadRepository.findOne(uuid.toString());

        /*
         * Check that the total size isn't too large!
         *
         * Cap it at 4GB for now.
         */
        long maxSize = 4l * 1024l * 1024l * 1024l;
        if (flowTotalSize > maxSize) {
            LOG.error("flowTotalSize: {} -- ", flowTotalSize, maxSize);
            throw new IOException("file to large");
        }

        if (upload == null) {
            /*
             * Entry didn't exist. Check to make sure we haven't already created
             * a file with the same UUID (highly unlikely).
             */
            if (testFileExists(uuid)) {
                throw new FileExistsException("file already exists");
            }

            /*
             * Create the new Upload entry and leave it as incomplete.
             */
            upload = new Upload();

            upload.setId(uuid.toString());
            upload.setOriginalName(flowFilename);
            upload.setUploadedAt(new LocalDate());
            upload.setUploadComplete(false);
            upload.setTotalChunks(flowTotalChunks);
            upload.setTotalSize(flowTotalSize);

            /*
             * Create the chunks list with all false.
             */
            List<Boolean> chunks = new ArrayList<Boolean>();

            for (int i = 0; i < flowTotalChunks; i++) {
                chunks.add(new Boolean(false));
            }

            upload.setChunks(chunks);

            /*
             * Create a directory to store the chunks in.
             */
            fileUtil.setDirectoryPaths(upload);
            fileUtil.createDirectory(upload.getChunkDirectory());

            upload = uploadRepository.save(upload);

        }

        /*
         * Test that the file hasn't changed since the last chunk test.
         */
        testFlowUnchanged(upload, flowFilename, flowTotalChunks, flowTotalSize);

        /*
         * Check if the chunk has already been uploaded.
         */
        if (upload.getChunks().get(flowChunkNumber - 1)) {
            LOG.warn("chunk already uploaded: {}", flowChunkNumber - 1);
            return true;
        }

        /*
         * We're all good. Let them send the chunk.
         */
        return false;
    }

    public String getAvailableIdentifier() {
        return getAvailableUUID().toString();
    }

    /**
     * Get's the next available (and random) UUID to assign as this flow's
     * unique identifier.
     *
     * @return
     */
    private UUID getAvailableUUID() {
        UUID uuid = null;
        while (uuid == null) {
            uuid = UUID.randomUUID();
            LOG.debug("trying uuid: {}", uuid);

            if (testFileExists(uuid)) {
                LOG.debug("uuid exists, trying again");
                uuid = null;
            }
        }
        return uuid;
    }

    /**
     * Does this file already exist on the filesystem?
     *
     * @param uuid
     *            the file UUID (name)
     *
     * @return
     */
    private boolean testFileExists(UUID uuid) {
        File f = new File(baseLocation + uuid.toString());
        return f.exists();
    }

    /**
     * Check that the flow hasn't changed at all. The first testChunk call gets
     * to set all the flow fields. After that, the owner, file name, total chunk
     * count, and total file size cannot change.
     *
     * @param upload
     * @param flowFilename
     * @param flowTotalChunks
     * @param flowTotalSize
     *
     * @throws IOException
     */
    private void testFlowUnchanged(Upload upload, String flowFilename,
            int flowTotalChunks, long flowTotalSize) throws IOException {

        if (!upload.getOriginalName().equals(flowFilename)) {
            throw new IOException("flowFilename change");
        }

        if (upload.getTotalChunks() != flowTotalChunks) {
            throw new IOException("flowTotalChunks change");
        }

        if (upload.getTotalSize() != flowTotalSize) {
            throw new IOException("flowTotalSize change");
        }
    }

    /**
     * Calculate the MD5SUM of the give file.
     *
     * @param flowIdentifier
     *
     * @return
     *
     * @throws IOException
     */
    @Timed
    private String calculateMd5Sum(String flowIdentifier) throws IOException {
        String md5 = null;

        /*
         * Generate an MD5 sum. This should be safe for large files.
         */
        FileInputStream fis = new FileInputStream(getFile(flowIdentifier));
        md5 = DigestUtils.md5Hex(fis);
        fis.close();

        return md5;
    }

    /**
     * Check the chunks list to see if they are all true. If they are, then all
     * the chunks are uploaded.
     *
     * @param u
     *
     * @return
     */
    private synchronized boolean uploadComplete(Upload u) {
        for (Boolean b : u.getChunks()) {
            if (b.booleanValue() == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Now that the chunks have all been uploaded, we need to merge them back
     * into the original file.
     *
     * @param upload
     *
     * @return
     *
     * @throws IOException
     */
    @Timed
    private synchronized Upload mergeChunks(Upload upload) throws IOException {
        upload = uploadRepository.findOne(upload.getId());
        fileUtil.merge(upload);
        return uploadRepository.save(upload);
    }

    /**
     * Get a File for this flow.
     *
     * @param flowIdentifier
     *
     * @return
     */
    private File getFile(String flowIdentifier) {
        return new File(fileUtil.getFileLocation(flowIdentifier));
    }

}
