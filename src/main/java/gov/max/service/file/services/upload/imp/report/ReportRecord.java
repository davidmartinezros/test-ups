package gov.max.service.file.services.upload.imp.report;

public class ReportRecord {
    private String identifier;
    private String message;
    private String details;
    private ReportOutcome outcome;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public ReportOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(ReportOutcome outcome) {
        this.outcome = outcome;
    }
}