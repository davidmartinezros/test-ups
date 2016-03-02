package gov.max.service.file.domain;

//import lombok.ToString;

//import org.springframework.batch.item.file.transform.Range;

//@ToString
public class Material {

    public String getProjectNo() {
        return projectNo;
    }

    public void setProjectNo(String projectNo) {
        this.projectNo = projectNo;
    }

    private String projectNo;

    public String getUnitArea() {
        return unitArea;
    }

    public void setUnitArea(String unitArea) {
        this.unitArea = unitArea;
    }

    public String getDesignArea() {
        return designArea;
    }

    public void setDesignArea(String designArea) {
        this.designArea = designArea;
    }

    public String getLineNo() {
        return lineNo;
    }

    public void setLineNo(String lineNo) {
        this.lineNo = lineNo;
    }

    public String getTrainSheetNo() {
        return trainSheetNo;
    }

    public void setTrainSheetNo(String trainSheetNo) {
        this.trainSheetNo = trainSheetNo;
    }

    public String getPipelineRef() {
        return pipelineRef;
    }

    public void setPipelineRef(String pipelineRef) {
        this.pipelineRef = pipelineRef;
    }

    public String getRevNo() {
        return revNo;
    }

    public void setRevNo(String revNo) {
        this.revNo = revNo;
    }

    public String getPipingClass() {
        return pipingClass;
    }

    public void setPipingClass(String pipingClass) {
        this.pipingClass = pipingClass;
    }

    public String getSpoolId() {
        return spoolId;
    }

    public void setSpoolId(String spoolId) {
        this.spoolId = spoolId;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getIdentCode() {
        return identCode;
    }

    public void setIdentCode(String identCode) {
        this.identCode = identCode;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getMatCategory() {
        return matCategory;
    }

    public void setMatCategory(String matCategory) {
        this.matCategory = matCategory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String unitArea;
    private String designArea;
    private String lineNo;
    private String trainSheetNo;
    private String pipelineRef;
    private String revNo;
    private String pipingClass;
    private String spoolId;
    private String size;
    private String identCode;
    private String quantity;
    private String uom;
    private String group;
    private String matCategory;
    private String description;

    public static String[] COLUMN_NAMES = new String[]{
            "projectNo",
            "unitArea",
            "designArea",
            "lineNo",
            "trainSheetNo",
            "pipelineRef",
            "revNo",
            "pipingClass",
            "spoolId",
            "size",
            "identCode",
            "quantity",
            "uom",
            "group",
            "matCategory",
            "description"
    };

//    public static Range[] COLUMN_RANGES = {
//            new Range(1, 3),
//            new Range(4, 14),
//            new Range(15, 21),
//            new Range(22, 34),
//            new Range(35, 38),
//            new Range(39, 84),
//            new Range(85, 88),
//            new Range(89, 94),
//            new Range(95, 130),
//            new Range(131, 137),
//            new Range(138, 148),
//            new Range(149, 155),
//            new Range(156, 159),
//            new Range(160, 168),
//            new Range(169, 175),
//            new Range(176, 284)
//    };

    public static String OUTPUT_LINE_FORMAT =
            "%-4s" +
            "%-10s" +
            "%-7s" +
            "%-13s" +
            "%-4s" +
            "%-46s" +
            "%-4s" +
            "%-6s" +
            "%-36s" +
            "%-7s" +
            "%-11s" +
            "%-7s" +
            "%-4s" +
            "%-9s" +
            "%-7s" +
            "%-109s";
}
