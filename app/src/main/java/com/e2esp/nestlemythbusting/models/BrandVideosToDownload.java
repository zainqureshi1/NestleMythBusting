package com.e2esp.nestlemythbusting.models;

import java.util.ArrayList;

/**
 * Created by Zain on 7/25/2017.
 */

public class BrandVideosToDownload {

    private String brand;
    private ArrayList<VideoToDownload> videosToDownload;

    public BrandVideosToDownload(String brand, ArrayList<VideoToDownload> videosToDownload) {
        this.brand = brand;
        this.videosToDownload = videosToDownload;
    }

    public String getBrand() {
        return brand;
    }

    public ArrayList<VideoToDownload> getVideosToDownload() {
        return videosToDownload;
    }

}
