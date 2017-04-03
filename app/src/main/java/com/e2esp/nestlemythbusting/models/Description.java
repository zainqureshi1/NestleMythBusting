package com.e2esp.nestlemythbusting.models;

/**
 * Created by Zain on 3/31/2017.
 */

public class Description {

    private String fileName;
    private String filePath;
    private String description;

    public Description(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.description = "";
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileNameWithoutExt() {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
