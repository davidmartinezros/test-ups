package gov.max.service.file.services.management;

import gov.max.service.file.domain.model.SharedLink;
import gov.max.service.file.domain.repositories.SharedLinkRepository;

import gov.max.service.file.util.FileUtil;
import gov.max.service.file.util.HttpResponseUtil;
import gov.max.service.file.security.SecurityUtils;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class FileOperationsService {

    private final Logger logger = LoggerFactory.getLogger(FileOperationsService.class);

    private
    @Value("${spring.repository.base.path}")
    String fileBasePath;

    private
    @Value("${spring.application.host}")
    String host;

    @Value("${application.virusScan.host}")
    private String scannerHost;

    @Value("${application.virusScan.port}")
    private int scannerPort;

    enum Mode {
        list, rename, copy, delete, savefile, editfile, addfolder, changepermissions, compress, extract
    }

    private String DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss z"; // (Wed, 4 Jul 2001 12:08:56)

    @Autowired
    private SharedLinkRepository sharedLinkRepository;

    @Autowired
    FileShareManagementService managementService;

    @Autowired
    HttpResponseUtil httpResponseUtil;

    @Autowired
    FileUtil fileUtil;

    @Autowired
    SecurityUtils securityUtils;

    public void fileOperation(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        JSONObject responseJsonObject = null;
        try {
            JSONObject params = getParams(request);
            Mode mode = Mode.valueOf(params.getString("mode"));
            switch (mode) {
                case addfolder:
                    responseJsonObject = addFolder(params);
                    break;
                case changepermissions:
                    responseJsonObject = changePermissions(params);
                    break;
                case compress:
                    responseJsonObject = compress(params);
                    break;
                case copy:
                    responseJsonObject = copy(params);
                    break;
                case delete:
                    responseJsonObject = delete(params);
                    break;
                case editfile: // get content
                    responseJsonObject = editFile(params);
                    break;
                case savefile: // save content
                    responseJsonObject = saveFile(params);
                    break;
                case extract:
                    responseJsonObject = extract(params);
                    break;
                case list:
                    responseJsonObject = list(params);
                    break;
                case rename:
                    responseJsonObject = rename(params);
                    break;
                default:
                    throw new ServletException("not implemented");
            }
            if (responseJsonObject == null) {
                responseJsonObject = httpResponseUtil.error("generic error : responseJsonObject is null");
            }
        } catch (Exception e) {
            responseJsonObject = httpResponseUtil.error(e.getMessage());
        }
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(responseJsonObject);
        out.flush();
    }

    public void uploadFile(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        // URL: $config.uploadUrl, Method: POST, Content-Type: multipart/form-data
        // Unlimited file upload, each item will be enumerated as file-1, file-2, etc.
        // [$config.uploadUrl]?destination=/public_html/image.jpg&file-1={..}&file-2={...}
        try {
            String destination = null;
            Map<String, InputStream> files = new HashMap<>();

            List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
            for (FileItem item : items) {
                if (item.isFormField()) {
                    // Process regular form field (input type="text|radio|checkbox|etc", select, etc).
                    if ("destination".equals(item.getFieldName())) {
                        destination = item.getString();
                    }
                } else {
                    // Process form file field (input type="file").
                    files.put(item.getName(), item.getInputStream());
                }
            }

            if (files.size() == 0) {
                throw new Exception("file size  = 0");
            } else {
                for (Map.Entry<String, InputStream> fileEntry : files.entrySet()) {
                    logger.info("\n\n ****** preparing to scan for viruses *********\n\n");
                    if (FileUtil.scan(fileEntry.getValue(), scannerHost, scannerPort)) {
                        File f = new File(fileBasePath + destination, fileEntry.getKey());
                        if (!fileUtil.write(fileEntry.getValue(), f)) {
                            throw new Exception("write error");
                        }
                    } else {
                        throw new Exception("File " + fileEntry.getKey() + " Failed virus scan.");
                    }
                }
            }
        } catch (FileUploadException e) {
            throw new ServletException("Cannot parse multipart request: DiskFileItemFactory.parseRequest", e);
        } catch (IOException e) {
            throw new ServletException("Cannot parse multipart request: item.getInputStream", e);
        } catch (Exception e) {
            throw new ServletException("Cannot write file", e);
        }
    }

    private JSONObject getParams(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = request.getReader();
        String str;
        while ((str = br.readLine()) != null) {
            sb.append(str);
        }
        br.close();
        JSONObject jObj = new JSONObject(sb.toString());
        JSONObject params = jObj.getJSONObject("params");
        params.put("userName", securityUtils.getUserDetails().getUsername());

        return params;
    }

    /**
     * List either a single share bucket/file or an entire MAX user's personal upload directory.
     */
    private JSONObject list(JSONObject params) throws ServletException {
        try {
            boolean onlyFolders = params.getBoolean("onlyFolders");
            Object val = params.get("publicId");
            if (!val.toString().equals("null")) {
                String publicId = params.getString("publicId");
                logger.debug("list publicId: {} onlyFolders: {}", publicId, onlyFolders);
                FileInfo fileInfo = managementService.info(publicId);
                File dir = new File(fileInfo.getFilePath(), "");
                return getDirectoryFiles(onlyFolders, dir, fileInfo);
            } else {
                return getSharedFiles(onlyFolders, params.getString("userName"));
            }
        } catch (Exception e) {
            logger.error("list", e);
            return httpResponseUtil.error(e.getMessage());
        }
    }

    private JSONObject getSharedFiles(boolean onlyFolders, String user) throws IOException {
        List<SharedLink> vals = sharedLinkRepository.findByCreatedBy(user);
        List<JSONObject> resultList = new ArrayList<>();
        SimpleDateFormat dt = new SimpleDateFormat(DATE_FORMAT);
        if (vals != null) {
            for (SharedLink model : vals) {

                JSONObject el = new JSONObject();
                if (model.getDeleted() != null && model.getExpired() != null && !model.getDeleted() && !model.getExpired()) {
                    File f = FileUtils.getFile(model.getFilePath(), model.getFileName());
                    if (!f.exists() || (onlyFolders && !f.isDirectory())) {
                        continue;
                    }
                    BasicFileAttributes attrs = Files.readAttributes(f.toPath(), BasicFileAttributes.class);

                    el.put("name", f.getName());
                    //                el.put("displayName", DateUtils.isTimeStampValid(model.getCreated().toString()) ?
                    //                        DateUtils.timeStampValue(model.getCreated().toString()) : model.getCreated().toString());
                    el.put("rights", getPermissions(f));
                    el.put("date", dt.format(new Date(attrs.lastModifiedTime().toMillis())));
                    el.put("size", f.length());
                    el.put("type", f.isFile() ? "file" : "dir");
                } else {
                    el.put("name", model.getFileName());
                    el.put("date", model.getCreated());
                    el.put("size", model.getFileSize());
                    el.put("type", model.getFileType());
                }

                Date expirationDate = model.getExpiration();
                if (expirationDate != null) {
                    Date date2 = new Date();
                    long diff = expirationDate.getTime() - date2.getTime();
                    long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                    el.put("expires", days > 1 ? days + " Days" : days + " Day");
                }

                el.put("createdBy", securityUtils.getUserDetails().getUsername());
                el.put("publicId", model.getPublicId());
                el.put("shareLink", host + "/share/" + model.getPublicId());
                el.put("session", fileUtil.extractFileNameFromUncPath(model.getFilePath()));
                el.put("deleted", model.getDeleted());
                el.put("expired", model.getExpired());

                resultList.add(el);
            }
        }

        return new JSONObject().put("result", resultList);
    }

    private JSONObject getDirectoryFiles(boolean onlyFolders, File dir, FileInfo info) throws IOException {
        File[] fileList = dir.listFiles();

        List<JSONObject> resultList = new ArrayList<>();
        SimpleDateFormat dt = new SimpleDateFormat(DATE_FORMAT);
        if (fileList != null) {
            // Calendar cal = Calendar.getInstance();
            for (File f : fileList) {
                if (!f.exists() || (onlyFolders && !f.isDirectory())) {
                    continue;
                }
                BasicFileAttributes attrs = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
                JSONObject el = new JSONObject();
                el.put("name", f.getName());
//                el.put("displayName", DateUtils.isTimeStampValid(f.getName()) ? DateUtils.timeStampValue(f.getName()) : f.getName());
                el.put("rights", getPermissions(f));
                el.put("date", dt.format(new Date(attrs.lastModifiedTime().toMillis())));
                el.put("size", f.length());
                el.put("type", f.isFile() ? "file" : "dir");
                el.put("createdBy", securityUtils.getUserDetails().getUsername());
                el.put("deleted", info.getDeleted());
                el.put("expired", info.getExpired());
                resultList.add(el);
            }
        }

        return new JSONObject().put("result", resultList);
    }

    private JSONObject rename(JSONObject params) throws ServletException {
        try {
            String path = params.getString("path");
            String newpath = params.getString("newPath");
            logger.debug("rename from: {} to: {}", path, newpath);

            File srcFile = new File(fileBasePath, path);
            File destFile = new File(fileBasePath, newpath);
            if (srcFile.isFile()) {
                FileUtils.moveFile(srcFile, destFile);
            } else {
                FileUtils.moveDirectory(srcFile, destFile);
            }
            return httpResponseUtil.success(params);
        } catch (Exception e) {
            logger.error("rename", e);
            return httpResponseUtil.error(e.getMessage());
        }
    }

    private JSONObject copy(JSONObject params) throws ServletException {
        try {
            String path = params.getString("path");
            String newpath = params.getString("newPath");
            logger.debug("copy from: {} to: {}", path, newpath);
            File srcFile = new File(fileBasePath, path);
            File destFile = new File(fileBasePath, newpath);
            if (srcFile.isFile()) {
                FileUtils.copyFile(srcFile, destFile);
            } else {
                FileUtils.copyDirectory(srcFile, destFile);
            }
            return httpResponseUtil.success(params);
        } catch (Exception e) {
            logger.error("copy", e);
            return httpResponseUtil.error(e.getMessage());
        }
    }

    private JSONObject delete(JSONObject params) throws ServletException {
        try {
            String path = params.getString("path");
            String session = params.getString("session");
            String publicId = params.getString("publicId");

            SharedLink model = sharedLinkRepository.findByPublicId(publicId);
            model.setDeleted(true);
            sharedLinkRepository.save(model);

            path = "/" + securityUtils.getUserDetails().getUsername() + "/" + session + path;
            logger.debug("delete {}", path);
            File srcFile = new File(fileBasePath, path);
            if (!FileUtils.deleteQuietly(srcFile)) {
                throw new Exception("Can't delete: " + srcFile.getAbsolutePath());
            }
            return httpResponseUtil.success(params);
        } catch (Exception e) {
            logger.error("delete", e);
            return httpResponseUtil.error(e.getMessage());
        }
    }

    private JSONObject editFile(JSONObject params) throws ServletException {
        // get content
        try {
            String path = params.getString("path");
            logger.debug("editFile path: {}", path);

            File srcFile = new File(fileBasePath, path);
            String content = FileUtils.readFileToString(srcFile);

            return new JSONObject().put("result", content);
        } catch (Exception e) {
            logger.error("editFile", e);
            return httpResponseUtil.error(e.getMessage());
        }
    }

    private JSONObject saveFile(JSONObject params) throws ServletException {
        // save content
        try {
            String path = params.getString("path");
            String content = params.getString("content");
            logger.debug("saveFile path: {} content: isNotBlank {}, size {}", path, StringUtils.isNotBlank(content), content != null ? content.length() : 0);

            File srcFile = new File(fileBasePath, path);
            FileUtils.writeStringToFile(srcFile, content);

            return httpResponseUtil.success(params);
        } catch (Exception e) {
            logger.error("saveFile", e);
            return httpResponseUtil.error(e.getMessage());
        }
    }

    private JSONObject addFolder(JSONObject params) throws ServletException {
        try {
            String path = params.getString("path");
            String name = params.getString("name");
            logger.debug("addFolder path: {} name: {}", path, name);
            File newDir = new File(fileBasePath + path, name);
            if (!newDir.mkdir()) {
                throw new Exception("Can't create directory: " + newDir.getAbsolutePath());
            }
            return httpResponseUtil.success(params);
        } catch (Exception e) {
            logger.error("addFolder", e);
            return httpResponseUtil.error(e.getMessage());
        }
    }

    private JSONObject changePermissions(JSONObject params) throws ServletException {
        try {
            String path = params.getString("path");
            String perms = params.getString("perms"); // "653"
            String permsCode = params.getString("permsCode"); // "rw-r-x-wx"
            boolean recursive = params.getBoolean("recursive");
            logger.debug("changepermissions path: {} perms: {} permsCode: {} recursive: {}", path, perms, permsCode, recursive);
            File f = new File(fileBasePath, path);
            setPermissions(f, permsCode, recursive);
            return httpResponseUtil.success(params);
        } catch (Exception e) {
            logger.error("changepermissions", e);
            return httpResponseUtil.error(e.getMessage());
        }
    }

    private JSONObject compress(JSONObject params) throws ServletException {
        try {
            String path = params.getString("path"); // "/public_html/compressed.zip"
            String destination = params.getString("destination"); // "/public_html/backups"
            // FIXME parameters are right? the doc so...
            logger.debug("compress path: {} destination: {}", path, destination);
            return httpResponseUtil.error("not implemented");
            // return success(params);
        } catch (Exception e) {
            logger.error("compress", e);
            return httpResponseUtil.error(e.getMessage());
        }
    }

    private JSONObject extract(JSONObject params) throws ServletException {
        try {
            String path = params.getString("path"); // "/public_html/compressed.zip"
            String destination = params.getString("destination"); // "/public_html/extracted-files"
            String sourceFile = params.getString("sourceFile"); // /public_html/compressed.zip"
            // FIXME parameters are right? the doc so...
            logger.debug("extract path: {} destination: {} sourceFile: {}", path, destination, sourceFile);
            return httpResponseUtil.error("not implemented");
        } catch (Exception e) {
            logger.error("extract", e);
            return httpResponseUtil.error(e.getMessage());
        }
    }

    private String getPermissions(File f) throws IOException {
        // http://www.programcreek.com/java-api-examples/index.php?api=java.nio.file.attribute.PosixFileAttributes
        PosixFileAttributeView fileAttributeView = Files.getFileAttributeView(f.toPath(), PosixFileAttributeView.class);
        PosixFileAttributes readAttributes = fileAttributeView.readAttributes();
        Set<PosixFilePermission> permissions = readAttributes.permissions();
        return PosixFilePermissions.toString(permissions);
    }

    private String setPermissions(File file, String permsCode, boolean recursive) throws IOException {
        // http://www.programcreek.com/java-api-examples/index.php?api=java.nio.file.attribute.PosixFileAttributes
        PosixFileAttributeView fileAttributeView = Files.getFileAttributeView(file.toPath(), PosixFileAttributeView.class);
        fileAttributeView.setPermissions(PosixFilePermissions.fromString(permsCode));
        if (file.isDirectory() && recursive && file.listFiles() != null) {
            for (File f : file.listFiles()) {
                setPermissions(f, permsCode, recursive);
            }
        }
        return permsCode;
    }
}
