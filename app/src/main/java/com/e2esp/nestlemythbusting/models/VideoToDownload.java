package com.e2esp.nestlemythbusting.models;

/**
 * Created by Zain on 7/25/2017.
 */

public class VideoToDownload {

    private String title;
    private String path;

    public VideoToDownload(String title, String path) {
        this.title = title;
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

}
