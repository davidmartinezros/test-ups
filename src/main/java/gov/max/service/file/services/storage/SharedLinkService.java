package gov.max.service.file.services.storage;

import gov.max.service.file.domain.model.SharedLink;
import gov.max.service.file.domain.model.Upload;
import gov.max.service.file.domain.repositories.SharedLinkRepository;
import gov.max.service.file.services.management.exception.EncryptionException;
import gov.max.service.file.util.EncryptionKey;
import gov.max.service.file.util.EncryptionUtil;
import gov.max.service.file.util.FileUtil;
import gov.max.service.file.security.SecurityUtils;

import org.javers.spring.annotation.JaversAuditable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class SharedLinkService {

    private static final Logger LOG = LoggerFactory.getLogger(SharedLinkService.class);

    private SharedLinkRepository sharedLinkRepository;

    @Autowired
    private FileStorageService storageService;

    @Autowired
    private EncryptionUtil encryptionUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileUtil fileUtil;

    @Autowired
    SecurityUtils securityUtils;

    private
    @Value("${spring.repository.base.path}")
    String fileBasePath;

    @Autowired
    public SharedLinkService(SharedLinkRepository sharedLinkRepository) {
        this.sharedLinkRepository = sharedLinkRepository;
    }

    public SharedLink findSharedLink(String id) {
        return sharedLinkRepository.findOne(id);
    }

    public List<SharedLink> findByCreatedBy(String createdBy) {
        return sharedLinkRepository.findByCreatedBy(createdBy);
    }

    public SharedLink findByPublicId(String publicId) {
        return sharedLinkRepository.findByPublicId(publicId);
    }

    @JaversAuditable
    public SharedLink save(SharedLink model) {
        return sharedLinkRepository.save(model);
    }

    public List<SharedLink> findByCreatedBefore(Instant created) {
        return sharedLinkRepository.findByCreatedBefore(created);
    }

    public SharedLink saveSharedModel(Upload upload, String password, String expiration) throws EncryptionException, FileNotFoundException {
        SharedLink test = sharedLinkRepository.findByStorageId(upload.getId());
        if (test == null) {
            String fileName = upload.getOriginalName();
            try {
                String filePath = upload.getFileDirectoryPath() + File.separator + upload.getOriginalName();
                EncryptionKey key = encryptionUtil.generateKey();
                InputStream inputStream = storageService.load(filePath);
//                InputStream encryptedStream = encryptionUtil.encryptStream(inputStream, key);

                String fileType = fileUtil.identifyFileTypeUsingFilesProbeContentType(filePath);
                String destination = upload.getFileDirectoryPath();
                String createdBy = securityUtils.getUserDetails().getUsername();
//                String storedFileId = storageService.save(inputStream, fileName, destination);

                SharedLink sharedLink = this.saveSharedLink(fileName,
                        fileType, upload.getTotalSize(), password,
                        /*storedFileId*/ upload.getId(), key, destination, createdBy,
                        Integer.parseInt(expiration));

                return sharedLink;
            } catch (NoSuchAlgorithmException e) {//| InvalidKeyException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
                LOG.error("EncryptionException: {}", e);
                throw new EncryptionException(e);
            }
        } else {
            LOG.info("SharedModel already exists: {}", test);
            return test;
        }
    }

    public SharedLink saveSharedLink(String fileName,
                                     String fileType,
                                     long fileSize,
                                     String password,
                                     String storageId,
                                     EncryptionKey key,
                                     String destination,
                                     String createdBy,
                                     Integer expiration) {
        String hashedPassword = password == null || "".equals(password) ? null : passwordEncoder.encode(password);
        String publicId = KeyGenerators.string().generateKey();
        SharedLink metaData = new SharedLink();
        metaData.setCreated(new Date());
        metaData.setPublicId(publicId);
        metaData.setStorageId(storageId);
        metaData.setFileName(fileName);
        metaData.setFileType(fileType);
        metaData.setFileSize(fileSize);
        metaData.setPassword(hashedPassword);
        metaData.setEncryptionKey(key.getData());
        metaData.setFilePath(destination);
        metaData.setCreatedBy(createdBy);
        metaData.setExpiration(expiration);
        return sharedLinkRepository.save(metaData);
    }

    public SharedLink getSharedLink(String publicId) {
        return sharedLinkRepository.findByPublicId(publicId);
    }

}
