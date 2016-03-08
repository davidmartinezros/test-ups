package gov.max.service.file.web.rest;

import com.canyapan.randompasswordgenerator.RandomPasswordGenerator;
import com.canyapan.randompasswordgenerator.RandomPasswordGeneratorException;

import com.codahale.metrics.annotation.Timed;

import gov.max.service.file.domain.model.SharedLink;
import gov.max.service.file.domain.model.Upload;
import gov.max.service.file.domain.repositories.SharedLinkRepository;
import gov.max.service.file.services.management.FileDownload;
import gov.max.service.file.services.management.FileShareManagementService;
import gov.max.service.file.services.management.FileOperationsService;
import gov.max.service.file.services.management.exception.EncryptionException;
import gov.max.service.file.services.management.exception.InvalidPasswordException;
import gov.max.service.file.services.management.exception.LinkExpiredException;
import gov.max.service.file.services.storage.SharedLinkService;
import gov.max.service.file.services.upload.UploadService;
import gov.max.service.file.services.upload.imp.ImporterControllerBase;
import gov.max.service.file.util.HttpResponseUtil;
import gov.max.service.file.web.model.ApiErrorModel;
import gov.max.service.file.security.SecurityUtils;
import gov.max.service.file.web.model.UploadFormModel;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing files.
 *
 * <p>This class accesses the User entity, and needs to fetch its collection of authorities.</p>
 * <p>
 * For a normal use-case, it would be better to have an eager relationship between User and Authority,
 * and send everything to the client side: there would be no DTO, a lot less code, and an outer-join
 * which would be good for performance.
 * </p>
 * <p>
 * We use a DTO for 3 reasons:
 * <ul>
 * <li>We want to keep a lazy association between the user and the authorities, because people will
 * quite often do relationships with the user, and we don't want them to get the authorities all
 * the time for nothing (for performance reasons). This is the #1 goal: we should not impact our users'
 * application because of this use-case.</li>
 * <li> Not having an outer join causes n+1 requests to the database. This is not a real issue as
 * we have by default a second-level cache. This means on the first HTTP call we do the n+1 requests,
 * but then all authorities come from the cache, so in fact it's much better than doing an outer join
 * (which will get lots of data from the database, for each HTTP call).</li>
 * <li> As this manages users, for security reasons, we'd rather have a DTO layer.</li>
 * </p>
 * <p>Another option would be to have a specific JPA entity graph to handle this case.</p>
 */
@Configuration
@RestController
public class ApiController extends ImporterControllerBase {

    enum Mode {
        list, rename, copy, delete, savefile, editfile, addfolder, changepermissions, compress, extract
    }

    private @Value("${spring.repository.base.path}")
    String fileBasePath;

    @Autowired
    private Logger log;

    @Inject
    private UploadService uploadService;

    @Inject
    FileShareManagementService managementService;

    @Autowired
    private SharedLinkRepository sharedLinkRepository;

    @Autowired
    FileOperationsService fileOperationsService;

    @Autowired
    HttpResponseUtil httpResponseUtil;

    @Autowired
    SecurityUtils securityUtils;

    @Autowired
    SharedLinkService sharedLinkService;

    private
    @Value("${spring.application.host}")
    String appHost;

//    @RequestMapping(value = "/share/files", method = RequestMethod.POST)
//    public ApiSuccessModel upload(@RequestParam(required = false) String password,
//                                  @RequestParam(required = true) String path,
//                                  @RequestParam("file") MultipartFile file,
//                                  HttpServletRequest request) throws Exception {
//
//        String publicId = shareService.upload(
//                file.getInputStream(),
//                file.getOriginalFilename(),
//                file.getContentType(),
//                file.getSize(),
//                password,
//                getUserDetails().getUsername(),
//                path);
//
//        return new ApiSuccessModel(request.getRequestURL() + "/" + publicId);
//    }

    /**
     * GET  /download -> download a file.
     */
    @RequestMapping(value = "/share/manage/*",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public Object download(@RequestParam(required = false) String mode,
                           @RequestParam(required = false) String preview,
                           @RequestParam(required = true) String publicId,
                           @RequestParam(required = true) String path,
                           @RequestHeader(value = "Authorization", required = false) String password,
                           HttpServletRequest request,
                           HttpServletResponse response) throws Exception {
        try {
            if (null != publicId) {
                FileDownload fileDownload = managementService.downloadClear(publicId);
                httpResponseUtil.writeFile(
                    response,
                    fileDownload.getStream(),
                    fileDownload.getFileName(),
                    fileDownload.getFileType(),
                    (int) fileDownload.getFileSize()
                );
            } else {
                fileOperationsService.fileOperation(request, response);
            }
            return null;
        } catch (InvalidPasswordException e) {
            return new ApiErrorModel("Invalid Password");
        } catch (LinkExpiredException e) {
            return new ApiErrorModel("Expired");
        }
    }

    /**
     * GET  /get -> get a file.
     */
    @RequestMapping(value = "/manage/*",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public Object get(@RequestParam(required = false) String mode,
                      @RequestParam(required = false) String preview,
                      @RequestParam(required = true) String publicId,
                      @RequestParam(required = true) String path,
                      @RequestHeader(value = "Authorization", required = false) String password,
                      HttpServletRequest request,
                      HttpServletResponse response) throws Exception {
        try {
            if (null != publicId && !publicId.isEmpty()) {
                FileDownload fileDownload = managementService.downloadClear(publicId);
                httpResponseUtil.writeFile(
                    response,
                    fileDownload.getStream(),
                    fileDownload.getFileName(),
                    fileDownload.getFileType(),
                    (int) fileDownload.getFileSize()
                );
            } else {
                fileOperationsService.fileOperation(request, response);
            }
            return null;
        } catch (InvalidPasswordException e) {
            return new ApiErrorModel("Invalid Password");
        } catch (LinkExpiredException e) {
            return new ApiErrorModel("Expired");
        }
    }

    /**
     * POST  /listFiles -> general operation on files.
     */
    @RequestMapping(value = "/share/manage/*",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void listFiles(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            if (ServletFileUpload.isMultipartContent(request)) {
                fileOperationsService.uploadFile(request, response);
            } else {
                fileOperationsService.fileOperation(request, response);
            }
        } catch (Throwable t) {
            httpResponseUtil.setError(t, response);
        }
    }

    /**
     * POST  /admin -> admin operations on a file.
     */
    @RequestMapping(value = "/manage/*",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void admin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String userName = securityUtils.getUserDetails().getUsername();
        try {
            if (ServletFileUpload.isMultipartContent(request)) {
                fileOperationsService.uploadFile(request, response);
            } else {
                request.setAttribute("userName", userName);
                fileOperationsService.fileOperation(request, response);
            }
        } catch (Throwable t) {
            httpResponseUtil.setError(t, response);
        }
    }

    /**
     * GET  /getPwd -> get a generated password.
     */
    @RequestMapping(value = "/api/generatepwd",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public Map getPwd() throws RandomPasswordGeneratorException {
        RandomPasswordGenerator passwordGenerator = new RandomPasswordGenerator()
                .withPasswordLength(8)
                .withLowerCaseCharacters(true)
                .withUpperCaseCharacters(true)
                .withDigits(true)
                .withSymbols(true)
                .withMinDigitCount(1)
                .withAvoidAmbiguousCharacters(true)
                .withForceEveryCharacterType(true);

        String password = passwordGenerator.generate();
        return Collections.singletonMap("response", password);
    }


    // =================== chunked file upload methods ============================

    /**
     * POST  /saveChunk -> save a chunk upload.
     *
     * The ng-flow requirements to handle upload of a single file chunk.
     *
     * @param uploadFormModel
     *     flowChunkNumber
     *     flowChunkSize
     *     flowCurrentChunkSize
     *     flowFilename
     *     flowIdentifier
     *     flowRelativePath
     *     flowTotalChunks
     *     flowTotalSize
     *     file
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/importer",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<?> saveChunk(@Valid UploadFormModel uploadFormModel, Model model,
                                       BindingResult bindingResult) throws Exception {

        log.info("/api/importer: POST: {} -- {} -- {}",
                uploadFormModel.getFlowIdentifier(), uploadFormModel.getFlowChunkNumber(), uploadFormModel.getFlowTotalSize());

        log.trace("flowChunkNumber: {}", uploadFormModel.getFlowChunkNumber());
        log.trace("flowChunkSize: {}", uploadFormModel.getFlowChunkSize());
        log.trace("flowCurrentChunkSize: {}", uploadFormModel.getFlowCurrentChunkSize());
        log.trace("flowFilename: {}", uploadFormModel.getFlowFilename());
        log.trace("flowIdentifier: {}", uploadFormModel.getFlowIdentifier());
        log.trace("flowRelativePath: {}", uploadFormModel.getFlowRelativePath());
        log.trace("flowTotalChunks: {}", uploadFormModel.getFlowTotalChunks());
        log.trace("flowTotalSize: {}", uploadFormModel.getFlowTotalSize());

        try {
            Upload u = uploadService.saveChunk(
                uploadFormModel.getFlowIdentifier(),
                uploadFormModel.getFlowChunkNumber(),
                uploadFormModel.getFlowChunkSize(),
                uploadFormModel.getFlowCurrentChunkSize(),
                uploadFormModel.getFlowFilename(),
                uploadFormModel.getFlowTotalChunks(),
                uploadFormModel.getFlowTotalSize(),
                uploadFormModel.getFile()
            );
            log.debug("upload: {}", u);
        } catch (IOException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(uploadFormModel.getFlowIdentifier(), HttpStatus.OK);
    }


    /**
     * GET  /testChunk -> test for chunk upload.
     *
     * The ng-flow requirements to handle the test of a single file chunk.
     *
     * @param flowChunkNumber
     * @param flowChunkSize - This option forces all chunks should be less than or equal to chunkSize. Otherwise,
     *                        it makes last chunk must be greater than or equal to chunkSize.

     * @param flowCurrentChunkSize
     * @param flowFilename
     * @param flowIdentifier
     * @param flowRelativePath
     * @param flowTotalChunks
     * @param flowTotalSize
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/importer",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<?> testChunk(
            @RequestParam("flowChunkNumber") int flowChunkNumber,
            @RequestParam("flowChunkSize") int flowChunkSize,
            @RequestParam("flowCurrentChunkSize") int flowCurrentChunkSize,
            @RequestParam("flowFilename") String flowFilename,
            @RequestParam("flowIdentifier") String flowIdentifier,
            @RequestParam("flowRelativePath") String flowRelativePath,
            @RequestParam("flowTotalChunks") int flowTotalChunks,
            @RequestParam("password") String password,
            @RequestParam("flowTotalSize") long flowTotalSize) throws Exception {

        log.trace("flowChunkNumber: {}", flowChunkNumber);
        log.trace("flowChunkSize: {}", flowChunkSize);
        log.trace("flowCurrentChunkSize: {}", flowCurrentChunkSize);
        log.trace("flowFilename: {}", flowFilename);
        log.trace("flowIdentifier: {}", flowIdentifier);
        log.trace("flowRelativePath: {}", flowRelativePath);
        log.trace("flowTotalChunks: {}", flowTotalChunks);
        log.trace("flowTotalSize: {}", flowTotalSize);

        boolean complete = false;

        try {
            complete = uploadService.testChunk(
                flowIdentifier,
                flowChunkNumber,
                flowChunkSize,
                flowCurrentChunkSize,
                flowFilename,
                flowTotalChunks,
                flowTotalSize
            );
        } catch (IOException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        /**
         * We return 200 when the chunk has already been
         * completed, a permanent error when we want to stop the upload, or
         * anything else when we want to allow the chunk to be uploaded.
         *
         * NOTE that even though the doc says anything else, apparently a 202
         * isn't going to work for the false case here. So change it to 502 and
         * it seems to work.
         */
        if (complete) {
            return new ResponseEntity<>(complete, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(complete, HttpStatus.BAD_GATEWAY);
        }
    }

    /**
     * GET  /getUniqueIdentifier -> get a unique identifier.
     *
     * Override the default ng-flow unique identifier function with a call so that we can
     * track progress. We will generate a new identifier for each file. This prevents multiple
     * users from uploading a file of the same name and having a collision.
     *
     * @return a new unique identifier
     */
    @RequestMapping(value = "/importer/getUniqueIdentifier",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public String getUniqueIdentifier() {
        return uploadService.getAvailableIdentifier();
    }

    /**
     * GET  /getUpload -> get the file upload.
     *
     * @param flowIdentifier
     */
    @RequestMapping(value = "/importer/upload/{flowIdentifier}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<?> getUpload(@PathVariable String flowIdentifier) {
        return Optional
                .ofNullable(uploadService.getUpload(flowIdentifier))
                .map(upload -> new ResponseEntity<>(upload, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * GET  /uploadSuccess -> trigger successful upload events.
     */
    @RequestMapping(value = "/importer/uploadSuccess",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public Map uploadSuccess(@RequestParam(required = true) String fileName,
                             @RequestParam(required = true) String uniqueIdentifier,
                             @RequestParam(required = false) String paused,
                             @RequestParam(required = false) String password,
                             @RequestParam(required = true) String expiration) {
        try {
            Upload upload = uploadService.getUpload(uniqueIdentifier);
            SharedLink sharedLink = sharedLinkService.saveSharedModel(upload, password, expiration);

            Map values = new HashMap();
            values.put("publicId", sharedLink.getPublicId());
            values.put("host", appHost);

            return Collections.singletonMap("response", values);

        } catch (EncryptionException | FileNotFoundException e) {
            log.error("Exception creating SharedLink Model: {}", e);
            return Collections.singletonMap("response", "Exception creating SharedLink Model");
        }
    }
}
