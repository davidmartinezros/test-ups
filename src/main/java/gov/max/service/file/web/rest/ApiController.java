package gov.max.service.file.web.rest;

import com.canyapan.randompasswordgenerator.RandomPasswordGenerator;
import com.canyapan.randompasswordgenerator.RandomPasswordGeneratorException;

import gov.max.service.file.domain.model.SharedLink;
import gov.max.service.file.domain.model.Upload;
import gov.max.service.file.domain.repository.SharedLinkRepository;
import gov.max.service.file.service.management.FileDownload;
import gov.max.service.file.service.management.FileShareManagementService;
import gov.max.service.file.service.management.FileOperationsService;
import gov.max.service.file.service.management.exception.EncryptionException;
import gov.max.service.file.service.management.exception.InvalidPasswordException;
import gov.max.service.file.service.management.exception.LinkExpiredException;
import gov.max.service.file.service.storage.SharedLinkService;
import gov.max.service.file.service.upload.UploadService;
import gov.max.service.file.service.upload.imp.ImporterControllerBase;
import gov.max.service.file.util.HttpResponseUtil;
import gov.max.service.file.web.model.ApiErrorModel;
import gov.max.service.file.util.SecurityUtils;
import gov.max.service.file.web.model.SuccessUploadFormModel;
import gov.max.service.file.web.model.UploadFormModel;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
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

@Configuration
@RestController
public class ApiController extends ImporterControllerBase {

    enum Mode {
        list, rename, copy, delete, savefile, editfile, addfolder, changepermissions, compress, extract
    }

    private
    @Value("${spring.repository.base.path}")
    String fileBasePath;

    @Autowired
    private Logger LOG;

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

    @RequestMapping(value = "/share/manage/*", method = RequestMethod.GET)
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

    @RequestMapping(value = "/manage/*", method = RequestMethod.GET)
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

    @RequestMapping(value = "/share/manage/*", method = RequestMethod.POST)
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

    // Admin operations
    @RequestMapping(value = "/manage/*", method = RequestMethod.POST)
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

    @RequestMapping(value = "/api/generatepwd", method = RequestMethod.GET, produces = "application/json")
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
    @RequestMapping(value = "/importer", method = RequestMethod.POST)
    public ResponseEntity<?> saveChunk(@Valid UploadFormModel uploadFormModel, Model model,
                                       BindingResult bindingResult) throws Exception {

        LOG.info("/api/importer: POST: {} -- {} -- {}",
                uploadFormModel.getFlowIdentifier(), uploadFormModel.getFlowChunkNumber(), uploadFormModel.getFlowTotalSize());

        LOG.trace("flowChunkNumber: {}", uploadFormModel.getFlowChunkNumber());
        LOG.trace("flowChunkSize: {}", uploadFormModel.getFlowChunkSize());
        LOG.trace("flowCurrentChunkSize: {}", uploadFormModel.getFlowCurrentChunkSize());
        LOG.trace("flowFilename: {}", uploadFormModel.getFlowFilename());
        LOG.trace("flowIdentifier: {}", uploadFormModel.getFlowIdentifier());
        LOG.trace("flowRelativePath: {}", uploadFormModel.getFlowRelativePath());
        LOG.trace("flowTotalChunks: {}", uploadFormModel.getFlowTotalChunks());
        LOG.trace("flowTotalSize: {}", uploadFormModel.getFlowTotalSize());

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
            LOG.debug("upload: {}", u);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(uploadFormModel.getFlowIdentifier(), HttpStatus.OK);
    }


    /**
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
    @RequestMapping(value = "/importer", method = RequestMethod.GET)
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

        LOG.trace("flowChunkNumber: {}", flowChunkNumber);
        LOG.trace("flowChunkSize: {}", flowChunkSize);
        LOG.trace("flowCurrentChunkSize: {}", flowCurrentChunkSize);
        LOG.trace("flowFilename: {}", flowFilename);
        LOG.trace("flowIdentifier: {}", flowIdentifier);
        LOG.trace("flowRelativePath: {}", flowRelativePath);
        LOG.trace("flowTotalChunks: {}", flowTotalChunks);
        LOG.trace("flowTotalSize: {}", flowTotalSize);

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
            LOG.error(e.getMessage());
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
     * Override the default ng-flow unique identifier function with a call so that we can
     * track progress. We will generate a new identifier for each file. This prevents multiple
     * users from uploading a file of the same name and having a collision.
     *
     * @return a new unique identifier
     */
    @RequestMapping(value = "/importer/getUniqueIdentifier", method = RequestMethod.GET)
    public String getUniqueIdentifier() {
        return uploadService.getAvailableIdentifier();
    }

    /**
     * Get the specified upload.
     *
     * @param flowIdentifier
     * @return
     */
    @RequestMapping(value = "/importer/upload/{flowIdentifier}", method = RequestMethod.GET)
    public ResponseEntity<?> getUpload(@PathVariable String flowIdentifier) {
        return Optional
                .ofNullable(uploadService.getUpload(flowIdentifier))
                .map(upload -> new ResponseEntity<>(upload, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @RequestMapping(value = "/importer/uploadSuccess", method = RequestMethod.GET)
    public Map uploadSuccess(@RequestParam(required = true) String fileName,
                             @RequestParam(required = true) String uniqueIdentifier,
                             @RequestParam(required = false) String paused,
                             @RequestParam(required = false) String password,
                             Model model) {
        try {
            Upload upload = uploadService.getUpload(uniqueIdentifier);
            SharedLink sharedLink = sharedLinkService.saveSharedModel(upload, password);

            Map values = new HashMap();
            values.put("publicId", sharedLink.getPublicId());
            values.put("host", appHost);

            return Collections.singletonMap("response", values);

        } catch (EncryptionException | FileNotFoundException e) {
            LOG.error("Exception creating SharedLink Model: {}", e);
            return Collections.singletonMap("response", "Exception creating SharedLink Model");
        }
    }
}
