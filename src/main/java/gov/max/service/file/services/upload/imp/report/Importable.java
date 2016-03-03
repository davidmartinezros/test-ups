package gov.max.service.file.services.upload.imp.report;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface Importable {
    List<ReportRecord> imp(File file);

    List<ReportRecord> imp(InputStream inputStream);
}