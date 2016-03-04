package gov.max.service.file.util;

import java.io.*;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import fi.solita.clamav.ClamAVClient;

import gov.max.service.file.domain.model.Upload;
import gov.max.service.file.domain.repositories.UploadRepository;

import gov.max.service.file.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.filefilter.TrueFileFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUtil {

    private static Logger logger = LoggerFactory.getLogger(FileUtil.class.getSimpleName());

    @Inject
    private UploadRepository uploadRepository;

    @Autowired
    SecurityUtils securityUtils;

    private
    @Value("${spring.repository.base.path}")
    String fileBasePath;

    /**
     * Extract a zip file and then store the file list of it to a indicated folder
     *
     * @param zipFile  the file path needs to be extract
     * @param toFolder store the file list to folder
     * @throws IOException
     */
    public static void extract(String zipFile, String toFolder) throws IOException {
        ZipUtils.unzip(zipFile, toFolder);
    }

    /**
     * Delete permanent a zip file out of storage
     *
     * @param zipFile the file path needs to be deleted
     * @throws IOException
     */
    public static void delete(String zipFile) throws IOException {
        org.apache.commons.io.FileUtils.forceDelete(new File(zipFile));
    }

    /**
     * Archive a zip file to a folder
     *
     * @param zipFile  the file path needs to be archived
     * @param toFolder store the file list to folder
     * @throws IOException
     */
    public static void archive(String zipFile, String toFolder) throws IOException {
        File file = new File(zipFile);
        org.apache.commons.io.FileUtils.copyFile(file, new File(toFolder + File.separator + file.getName()));
        org.apache.commons.io.FileUtils.forceDelete(file);
    }

    /**
     * List all files from an indicated folder
     *
     * @param fromFolder the folder contains the list of file
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static List<String> listFiles(String fromFolder) throws IOException {
        List<String> fileList = new ArrayList<String>();
        File dir = new File(fromFolder);

        List<File> files = (List<File>) org.apache.commons.io.FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        for (File file : files) {
            fileList.add(file.getCanonicalPath());
        }
        return fileList;
    }

    /**
     * Zip a file to a zip file
     *
     * @param fromFile
     * @param toFile
     * @throws IOException
     */
    public static void zip(String fromFile, String toFile) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(toFile);
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

        ZipEntry zipEntry = new ZipEntry(new File(fromFile).getName());
        zipOutputStream.putNextEntry(zipEntry);

        FileInputStream fileInputStream = new FileInputStream(fromFile);
        byte[] buf = new byte[1024];
        int bytesRead;

        while ((bytesRead = fileInputStream.read(buf)) > 0) {
            zipOutputStream.write(buf, 0, bytesRead);
        }

        fileInputStream.close();
        zipOutputStream.closeEntry();
        zipOutputStream.close();
        fileOutputStream.close();
    }

    /**
     * Save file from base 64 content
     *
     * @param fileDir
     * @param fileName
     * @param based64Content
     * @throws IOException
     */
    public static void saveFile(String fileDir, String fileName, String based64Content) throws Exception {
        File destDir = new File(fileDir);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        String filePath = fileDir + fileName;
        File file = new File(filePath);
        byte dearr[] = Base64.decodeBase64(based64Content.getBytes());
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(dearr);
        fos.flush();
        fos.close();
    }

    /**
     * Get base 64 data of file
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    public static String getFileDataBased64(String filePath) throws Exception {
        int data;
        File file = new File(filePath);
        FileReader fr = new FileReader(file);
        ByteArrayOutputStream bf = new ByteArrayOutputStream();
        while ((data = fr.read()) != -1) {
            bf.write(data);
        }
        byte content[] = Base64.encodeBase64(bf.toByteArray());
        String ret = new String(content);
        bf.close();
        fr.close();
        return ret;
    }

    public static String extractFileNameFromUncPath(String fileName) {
        String pattern = "[^\\\\/:*?\"<>|\\r\\n]+$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(fileName);
        if (m.find()) {
            return m.group(0);
        } else {
            return fileName;
        }
    }

    // TODO: find usages and fix - should create so many folders
    public static String createSessionFolder(String userPath) {
        try {
            logger.debug("createSessionFolder path: {}", userPath);
            File newDir = new File(userPath, timeMillisecond());
            if (!newDir.mkdir()) {
                logger.error("createSessionFolder", "Failed to create directory.");
            }
            return newDir.getAbsolutePath();
        } catch (Exception e) {
            logger.error("createSessionFolder", e);
            return null;
        }
    }

    public static String userFolder(String basePath, String userName) {
        try {
            logger.info("userFolder path: {} name: {}", basePath, userName);
            File newDir = new File(basePath, userName);
            if (!newDir.mkdir()) {
                logger.warn("userFolder", "Directory Already Exists.");
                return newDir.getAbsolutePath();
            }
            return newDir.getAbsolutePath();
        } catch (Exception e) {
            logger.error("userFolder", e);
            return null;
        }
    }

    public static List<FileItem> extractZipToListFiles(String zipFileName, String toFolder) throws Exception {
        File zipFile = new File(zipFileName);
        List<FileItem> ret = new ArrayList<>();
        String zipFileBase64 = getFileDataBased64(zipFileName);
        saveFile(toFolder, zipFile.getName(), zipFileBase64);
        extract(zipFileName, toFolder);

        List<String> listFiles = listFiles(toFolder);
        for (String filePath : listFiles) {
            if (!filePath.equalsIgnoreCase(zipFileName)) {
                File file = new File(filePath);
                FileItem fileItem = new FileItem();
                fileItem.setFileName(file.getCanonicalPath());
                fileItem.setBase64Content(getFileDataBased64(filePath));
                fileItem.setMimeType(new MimetypesFileTypeMap().getContentType(file));

                ret.add(fileItem);
            }
        }

        return ret;
    }

    /**
     * Write the new chunk to disk and update the Upload record. Don't
     * synchronize here because the file write may take some time. Only
     * synchronize on the update of the Upload.
     *
     * @param u
     * @param flowChunkNumber
     * @param file
     *
     * @return
     *
     * @throws IOException
     */
    public Upload saveChunkToDisk(Upload u, int flowChunkNumber, MultipartFile file) throws IOException {
        File f = getChunkFile(u, flowChunkNumber);

        FileOutputStream output = new FileOutputStream(f, true);

        try {
            output.write(file.getBytes());
        } finally {
            output.close();
        }

        return updateUploadForChunk(u, flowChunkNumber);
    }

    /**
     * Force the update of the Upload through here so that we can avoid
     * optimistic update exceptions.
     *
     * @param u
     * @param flowChunkNumber
     *
     * @return
     */
    private synchronized Upload updateUploadForChunk(Upload u, int flowChunkNumber) {
        u = uploadRepository.findOne(u.getId());
        u.getChunks().set(flowChunkNumber - 1, new Boolean(true));
        u = uploadRepository.save(u);

        return u;
    }

    /**
     * Get the full path for this flow.
     *
     * @param flowIdentifier
     *
     * @return the file location
     */
    public String getFileLocation(String flowIdentifier) {
        Upload upload = uploadRepository.findOne(flowIdentifier);
        return upload.getFileDirectoryPath() + File.separator + upload.getId();
    }

    /**
     * Get the full path for this file.
     *
     * @param upload
     *
     * @return the file location
     */
    public String getFileChunkLocationByName(Upload upload) {
        return upload.getChunkDirectory() + File.separator;
    }

    /**
     * Get the full path for this file.
     *
     * @param upload
     *
     * @return the file location
     */
    public String getFileChunkByName(Upload upload) {
        return upload.getChunkDirectory() + File.separator + upload.getId();
    }

    public boolean merge(Upload upload) throws IOException {
        FileOutputStream flowFile = new FileOutputStream(upload.getFileDirectoryPath() + "/" + upload.getId());

        for (int i = 1; i <= upload.getTotalChunks(); i++) {
            byte[] chunkBytes;

            try {
                chunkBytes = getChunkBytes(upload, i);
                flowFile.write(chunkBytes);

                File chunkFile = getChunkFile(upload, i);
                chunkFile.delete();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }

        flowFile.close();

        File chunkDir = new File(getChunkDirectoryPath(upload));
        chunkDir.delete();

        return true;
    }

    /**
     * Get a File for this chunk of the flow.
     *
     * @param upload
     * @param flowChunkNumber
     *
     * @return
     */
    private File getChunkFile(Upload upload, int flowChunkNumber) {
        return new File(getChunkDirectoryPath(upload) + flowChunkNumber);
    }

    /**
     * Given the Upload, get the path that the chunks should be placed in.
     *
     * @param upload
     *
     * @return the chunk directory path
     */
    public String getChunkDirectoryPath(Upload upload) {
        return upload.getChunkDirectory() + File.separator;
    }

    /**
     * Given the Upload, get the path that the chunks should be placed in.
     *
     * @param upload
     *
     * @return the upload
     */
    public Upload setDirectoryPaths(Upload upload) {

        String sessionDirectory = FileUtil
                .createSessionFolder(this.userFolder(fileBasePath, securityUtils.getUserDetails().getUsername()));

        String chunkDirectory = sessionDirectory
                + File.separator +
                upload.getId().toString() + ".chunk"
                + File.separator;

        upload.setFileDirectoryPath(sessionDirectory);
        upload.setChunkDirectory(chunkDirectory);

        return upload;
    }

    /**
     * If the base location doesn't exist on disk, then go ahead and create it
     * creating any parent directories as needed.
     *
     * @throws IOException
     */
    public synchronized void createBaseLocation() throws IOException {
        createDirectory(fileBasePath);
    }

    /**
     * Create the named directory and any parent directories.
     *
     * @param directoryPath
     *            the directory path to create
     *
     * @throws IOException
     */
    public synchronized void createDirectory(String directoryPath) throws IOException {
        File dir = new File(directoryPath);

        if (!dir.exists()) {
            try {
                dir.mkdirs();
                return;
            } catch (SecurityException e) {
                logger.error(e.getMessage(), e);
                throw new IOException(e);
            }
        } else {
            if (!dir.isDirectory()) {
                /**
                 * It exists but is a file?
                 */
                throw new IOException(String.format(
                        "File name exists and is not a directory: %s",
                        directoryPath)
                );
            }
        }
    }

    /**
     * Identify file type of file with provided path and name
     * using JDK 7's Files.probeContentType(Path).
     *
     * @param fileName Name of file whose type is desired.
     * @return String representing identified type of file with provided name.
     */
    public String identifyFileTypeUsingFilesProbeContentType(final String fileName)
    {
        String fileType = "Undetermined";
        final File file = new File(fileName);
        try
        {
            fileType = Files.probeContentType(file.toPath());
        }
        catch (IOException ioException)
        {
            logger.error("ERROR: Unable to determine file type for " + fileName
                            + " due to exception " + ioException);
        }
        return fileType;
    }

    /**
     * Get the bytes from the chunk of the flow.
     *
     * @param upload
     * @param flowChunkNumber
     *
     * @return
     *
     * @throws IOException
     */
    private byte[] getChunkBytes(Upload upload, int flowChunkNumber) throws IOException {
        byte[] rtn = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        File f = getChunkFile(upload, flowChunkNumber);

        if (f.exists()) {
            try {
                fis = new FileInputStream(f);
                bis = new BufferedInputStream(fis);
                rtn = new byte[(int) f.length()];
                bis.read(rtn);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                return null;
            } finally {
                bis.close();
            }
        }

        return rtn;
    }

    private static String timeMillisecond() throws ParseException {
        Date date = new Date();
        return String.valueOf(date.getTime());
    }

    public static boolean scan(InputStream is, String scannerHost, int scannerPort) {
        if (is != null) {
            try {
                ClamAVClient a = new ClamAVClient(scannerHost, scannerPort);
                byte[] r = a.scan(is);
                return ClamAVClient.isCleanReply(r);
            } catch(IOException e) {
                logger.error("Error running virus scan: {}", e);
                return false;
            }
        } else return false;
    }

    public boolean write(InputStream inputStream, File f) {
        boolean ret = false;

        // http://www.mkyong.com/java/how-to-convert-inputstream-to-file-in-java/
        OutputStream outputStream = null;

        try {
            // write the inputStream to a FileOutputStream
            outputStream = new FileOutputStream(f);

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            ret = true;

        } catch (IOException e) {
            logger.error("", e);

        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
            if (outputStream != null) {
                try {
                    // outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    logger.error("", e);
                }

            }
        }
        return ret;
    }

}
