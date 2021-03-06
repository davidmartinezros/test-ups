package gov.max.service.file.services.management;

import gov.max.service.file.services.management.exception.EncryptionException;
import gov.max.service.file.domain.model.SharedLink;
import gov.max.service.file.services.management.exception.InvalidPasswordException;
import gov.max.service.file.services.management.exception.LinkExpiredException;
import gov.max.service.file.services.storage.FileStorageService;
import gov.max.service.file.services.storage.SharedLinkService;
import gov.max.service.file.util.EncryptionKey;
import gov.max.service.file.util.EncryptionUtil;

import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class FileShareManagementService {

    @Autowired
    private Logger log;

    private
    @Value("${spring.repository.base.path}")
    String fileBasePath;

    @Autowired
    private FileStorageService storageService;

    @Autowired
    private EncryptionUtil encryptionUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SharedLinkService sharedLinkService;

    public String upload(InputStream inputStream, String fileName, String fileType, long fileSize, String password, String createdBy, String destination, Integer expiration) throws EncryptionException {
        try {
            EncryptionKey key = encryptionUtil.generateKey();
            InputStream encryptedStream = encryptionUtil.encryptStream(inputStream, key);
            String storedFileId = storageService.save(encryptedStream, fileName, destination);
            SharedLink sharedLink = sharedLinkService.saveSharedLink(fileName, fileType, fileSize, password, storedFileId, key, destination, createdBy, expiration);

            log.info(String.format("Upload %s with id %s", fileName, sharedLink.getId()));
            return sharedLink.getPublicId();
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
            throw new EncryptionException(e);
        }
    }

    public FileInfo info(String publicId) throws LinkExpiredException {
        SharedLink sharedLink = sharedLinkService.findByPublicId(publicId);
        if (sharedLink == null) {
            log.info(String.format("Link %s is expired", publicId));
            throw new LinkExpiredException();
        } else {
            log.info(String.format("Info %s", sharedLink.getId()));
            return new FileInfo(sharedLink.getFileName(),
                    sharedLink.getFilePath(),
                    sharedLink.getCreatedBy(),
                    sharedLink.getFileSize(),
                    sharedLink.getPassword() != null,
                    sharedLink.getDeleted(),
                    sharedLink.getExpired());
        }
    }

    public FileInfo infoProtected(String publicId, String password) throws InvalidPasswordException, LinkExpiredException {
        SharedLink sharedLink = sharedLinkService.findByPublicId(publicId);
        if (sharedLink == null) {
            log.info(String.format("Link %s is expired", publicId));
            throw new LinkExpiredException();
        } else if (sharedLink.getPassword() != null && (password == null || !passwordEncoder.matches(password, sharedLink.getPassword()))) {
            log.info(String.format("Invalid password for download %s", sharedLink.getId()));
            throw new InvalidPasswordException();
        } else {
            log.info(String.format("Info %s", sharedLink.getId()));
            return new FileInfo(sharedLink.getFileName(),
                    sharedLink.getFilePath(),
                    sharedLink.getCreatedBy(),
                    sharedLink.getFileSize(),
                    sharedLink.getPassword() != null,
                    sharedLink.getDeleted(),
                    sharedLink.getExpired());
        }
    }

    public FileDownload download(String publicId, String password) throws InvalidPasswordException, LinkExpiredException, EncryptionException {
        SharedLink sharedLink = sharedLinkService.findByPublicId(publicId);
        if (sharedLink == null) {
            log.info(String.format("Link %s is expired", publicId));
            throw new LinkExpiredException();
        } else if (sharedLink.getPassword() != null && (password == null || !passwordEncoder.matches(password, sharedLink.getPassword()))) {
            log.info(String.format("Invalid password for download %s", sharedLink.getId()));
            throw new InvalidPasswordException();
        } else {
            return getFileDownload(sharedLink);
        }
    }

    public FileDownload downloadClear(String publicId) throws InvalidPasswordException, LinkExpiredException, EncryptionException {
        SharedLink sharedLink = sharedLinkService.findByPublicId(publicId);
        if (sharedLink == null) {
            log.info(String.format("Link %s is expired", publicId));
            throw new LinkExpiredException();
        } else {
            return getFileDownload(sharedLink);
        }
    }

    private FileDownload getFileDownload(SharedLink sharedLink) throws LinkExpiredException, EncryptionException {
        try {
            EncryptionKey key = encryptionUtil.generateKey(sharedLink.getEncryptionKey());
            InputStream encryptedStream = storageService.load(sharedLink.getFilePath() + "/" + sharedLink.getFileName());
            InputStream stream = encryptionUtil.decryptStream(encryptedStream, key);
            log.info(String.format("Download %s", sharedLink.getId()));
            return new FileDownload(
                    encryptedStream,
                    sharedLink.getFilePath(),
                    sharedLink.getFileName(),
                    sharedLink.getFileType(),
                    sharedLink.getFileSize(),
                    sharedLink.getCreatedBy());
        } catch (FileNotFoundException e) {
            log.info(String.format("File for download %s is missing", sharedLink.getId()));
            throw new LinkExpiredException();
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
            throw new EncryptionException(e);
        }
    }
}
