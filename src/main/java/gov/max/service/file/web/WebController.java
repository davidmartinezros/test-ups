package gov.max.service.file.web;

import gov.max.service.file.domain.model.SharedLink;
import gov.max.service.file.domain.model.Upload;
import gov.max.service.file.service.management.exception.EncryptionException;
import gov.max.service.file.service.storage.SharedLinkService;
import gov.max.service.file.service.upload.UploadService;
import gov.max.service.file.web.model.SuccessUploadFormModel;
import gov.max.service.file.web.model.UploadFormModel;
import gov.max.service.file.service.management.FileShareManagementService;
import gov.max.service.file.service.management.exception.InvalidPasswordException;
import gov.max.service.file.service.management.exception.LinkExpiredException;
import gov.max.service.file.util.FileUtil;
import gov.max.service.file.util.HttpResponseUtil;
import gov.max.service.file.service.management.FileInfo;
import gov.max.service.file.util.SecurityUtils;

import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.FileNotFoundException;
import java.util.*;

@Configuration
@Controller
public class WebController extends WebMvcConfigurerAdapter {

    @Autowired
    private Logger LOG;

    @Inject
    private Environment env;

    private
    @Value("${spring.repository.base.path}")
    String fileBasePath;

    private
    @Value("${spring.application.host}")
    String appHost;

    @Value("${application.virusScan.host}")
    private String scannerHost;

    @Value("${application.virusScan.port}")
    private int scannerPort;

    @Inject
    private UploadService uploadService;

    @Inject
    FileShareManagementService managementService;

    @Autowired
    HttpResponseUtil httpResponseUtil;

    @Autowired
    FileUtil fileUtils;

    @Autowired
    SecurityUtils securityUtils;

    @Autowired
    private SharedLinkService sharedLinkService;

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public String showUpload(UploadFormModel uploadFormModel) {
        return "upload";
    }

    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public String admin(UploadFormModel uploadFormModel) {
        return "admin";
    }

    /**
     * This method supports file upload using two strategies.
     *
     * Traditional Uploads - single file and complete file object (non-chunked)
     *   - Validation: check binding results from Spring auto-bind to model (UploadFormModel)
     *   -
     * @param uploadFormModel
     * @param model
     * @param bindingResult
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String uploadFile(@Valid UploadFormModel uploadFormModel, Model model,
                             BindingResult bindingResult) throws Exception {

        if (bindingResult.hasErrors()) {
            return "upload";
        } else if (uploadFormModel.getFile().isEmpty()) {
            bindingResult.addError(new ObjectError("file", "No file specified"));
            return "upload";
        } else {
            MultipartFile file = uploadFormModel.getFile();

            boolean good = false;
            Collection activeProfiles = Arrays.asList(env.getActiveProfiles());
            if (activeProfiles.contains("dev")) {
                good = true;
            } else {
                good = FileUtil.scan(file.getInputStream(), scannerHost, scannerPort);
            }

            if (good) {
                String publicId = managementService.upload(
                    file.getInputStream(),
                    FileUtil.extractFileNameFromUncPath(file.getOriginalFilename()),
                    file.getContentType(),
                    file.getSize(),
                    uploadFormModel.getPassword(),
                    securityUtils.getUserDetails().getUsername(),
                    FileUtil.createSessionFolder(fileUtils.userFolder(fileBasePath, securityUtils.getUserDetails().getUsername()))
                );

                model.addAttribute("publicId", publicId);
                model.addAttribute("host", appHost);

                return "success";
            } else {
                return "scanfail";
            }
        }
    }

    @RequestMapping(value = "/share/{publicId:[a-z0-9]{16}}", method = RequestMethod.GET)
    public String showDownload(@PathVariable String publicId, Model model) {
        try {
            FileInfo fileInfo = managementService.info(publicId);
            model.addAttribute("file", fileInfo);
            model.addAttribute("publicId", publicId);
            if (fileInfo.isPasswordProtected()) {
                return "download";
            } else {
                return "manage";
            }
        } catch (LinkExpiredException e) {
            return "expired";
        }
    }

    @RequestMapping(value = "/share/{publicId:[a-z0-9]{16}}", method = RequestMethod.POST)
    public String download(@PathVariable String publicId,
                           @RequestParam(required = false) String password,
                           Model model) throws Exception {
        try {
            FileInfo fileInfo = managementService.infoProtected(publicId, password);
            model.addAttribute("file", fileInfo);
            model.addAttribute("publicId", publicId);
            return "manage";
        } catch (InvalidPasswordException e) {
            FileInfo fileInfo = managementService.info(publicId);
            model.addAttribute("file", fileInfo);
            model.addAttribute("pwError", true);
            return "download";
        } catch (LinkExpiredException e) {
            return "expired";
        }
    }

    @RequestMapping(value = "/api/listUrl/{publicId:[a-z0-9]{16}}", method = RequestMethod.POST)
    public String list(@PathVariable String publicId,
                       @RequestParam(required = false) String password, Model model,
                       HttpServletResponse response) throws Exception {
        return "expired";
    }

    @RequestMapping(value = "/uploadComplete", method = RequestMethod.GET)
    public String success(@RequestParam(required = true) String publicId, Model model) {
        model.addAttribute("publicId", publicId);
        model.addAttribute("host", appHost);
        return "success";
    }

}
