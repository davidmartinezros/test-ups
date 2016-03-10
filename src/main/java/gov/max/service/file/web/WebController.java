package gov.max.service.file.web;

import com.codahale.metrics.annotation.Timed;

import gov.max.service.file.services.storage.SharedLinkService;
import gov.max.service.file.services.upload.UploadService;
import gov.max.service.file.web.model.UploadFormModel;
import gov.max.service.file.services.management.FileShareManagementService;
import gov.max.service.file.services.management.exception.InvalidPasswordException;
import gov.max.service.file.services.management.exception.LinkExpiredException;
import gov.max.service.file.util.FileUtil;
import gov.max.service.file.util.HttpResponseUtil;
import gov.max.service.file.services.management.FileInfo;
import gov.max.service.file.security.SecurityUtils;

import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
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
import java.util.*;

@Configuration
@Controller
public class WebController extends WebMvcConfigurerAdapter {

    @Autowired
    private Logger LOG;

    @Inject
    private Environment env;

    private @Value("${spring.repository.base.path}")
    String fileBasePath;

    private @Value("${spring.application.host}")
    String appHost;

    private @Value("${application.virusScan.host}")
    String scannerHost;

    private @Value("${application.virusScan.port}")
    int scannerPort;

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

    /**
     * GET  /showUpload -> display upload page.
     */
    @RequestMapping(value = "/create",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public String showUpload(UploadFormModel uploadFormModel) {
        return "upload";
    }

    /**
     * GET  /admin -> display admin page.
     */
     @RequestMapping(value = "/admin",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_XHTML_XML_VALUE)
    @Timed
    public String admin(UploadFormModel uploadFormModel) {
        return "admin";
    }

    /**
     * POST  /uploadFile -> Create a new file.
     *
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
    @RequestMapping(value = "/create",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_XHTML_XML_VALUE)
    @Timed
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
                    FileUtil.createSessionFolder(fileUtils.userFolder(fileBasePath, securityUtils.getUserDetails().getUsername())),
                    uploadFormModel.getExpiration()
                );

                model.addAttribute("publicId", publicId);
                model.addAttribute("host", appHost);

                return "success";
            } else {
                return "scanfail";
            }
        }
    }

    /**
     * GET  /showDownload -> display download page.
     */
    @RequestMapping(value = "/share/{publicId:[a-z0-9]{16}}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_XHTML_XML_VALUE)
    @Timed
    public String showDownload(@PathVariable String publicId, Model model) {
        try {
            FileInfo fileInfo = managementService.info(publicId);
//            String createdBy = fileInfo.getCreatedBy();
//            String userName = securityUtils.getUserDetails().getUsername();
//            boolean owner = userName == null ? false : createdBy.equalsIgnoreCase(userName);

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

    /**
     * GET  /download -> display manage page.
     */
    @RequestMapping(value = "/share/{publicId:[a-z0-9]{16}}",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_XHTML_XML_VALUE)
    @Timed
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

    /**
     * POST  /list -> display expired page.
     */
    @RequestMapping(value = "/api/listUrl/{publicId:[a-z0-9]{16}}",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_XHTML_XML_VALUE)
    @Timed
    public String list(@PathVariable String publicId,
                       @RequestParam(required = false) String password, Model model,
                       HttpServletResponse response) throws Exception {
        return "expired";
    }

    /**
     * GET  /success -> display success page.
     */
    @RequestMapping(value = "/uploadComplete",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_XHTML_XML_VALUE)
    @Timed
    public String success(@RequestParam(required = true) String publicId, Model model) {
        model.addAttribute("publicId", publicId);
        model.addAttribute("host", appHost);
        return "success";
    }

}
