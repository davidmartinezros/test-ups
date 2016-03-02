package gov.max.service.file.web.model;

public class ApiSuccessModel {

    private String url;

    public ApiSuccessModel(String url) {
        this.setUrl(url);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
