package gov.max.service.file.web.model;

import java.io.Serializable;

/**
 * Need to model this:
 * <p/>
 * {"params":{"mode":"list","onlyFolders":false,"path":"/"}}
 */
public class ListFormModel implements Serializable {

    private Params params;

    public void setParams(Params params) {
        this.params = params;
    }

    public Params getParams() {
        return this.params;
    }

    public class Params {
        private String mode;
        private String onlyFolders;
        private String path;

        public void setMode(String mode) {
            this.mode = mode;
        }

        public void setOnlyFolders(String onlyFolders) {
            this.onlyFolders = onlyFolders;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getMode() {
            return this.mode;
        }

        public String getOnlyFolders() {
            return this.onlyFolders;
        }

        public String getPath() {
            return this.path;
        }
    }

}
