package gov.max.service.file.web.model;

public class ApiErrorModel {

    private String error;

    public ApiErrorModel(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
