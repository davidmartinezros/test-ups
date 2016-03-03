package gov.max.service.file.web.rest;

import gov.max.service.file.services.management.FileShareManagementService;
import gov.max.service.file.services.upload.UploadService;
import gov.max.service.file.services.upload.imp.ImporterControllerBase;
import gov.max.service.file.util.HttpResponseUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

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
