package gov.max.service.file.service.upload.imp;

import gov.max.service.file.service.upload.imp.report.Importable;
import gov.max.service.file.service.upload.imp.report.ReportRecord;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public class FileUploadBean implements Importable {

    @Override
    public List<ReportRecord> imp(File file) {
        return null;
    }

    @Override
    public List<ReportRecord> imp(InputStream inputStream) {
        return null;
    }
}
