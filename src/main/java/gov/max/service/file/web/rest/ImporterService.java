package gov.max.service.file.web.rest;

import gov.max.service.file.web.model.UploadFormModel;
import gov.max.service.file.domain.model.Upload;
import gov.max.service.file.service.management.FileShareManagementService;
import gov.max.service.file.service.upload.UploadService;
import gov.max.service.file.service.upload.imp.ImporterControllerBase;
import gov.max.service.file.util.HttpResponseUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Optional;

/**
 * Specifically designed to handle the ng-flow/flow.js upload library.
 */
@RestController
@RequestMapping("/api")
public class ImporterService extends ImporterControllerBase {

    private final static Logger LOG = LoggerFactory.getLogger(ImporterService.class);

    @Inject
    private UploadService uploadService;

    @Inject
    FileShareManagementService managementService;

    @Inject
    HttpResponseUtil httpResponseUtil;


}
