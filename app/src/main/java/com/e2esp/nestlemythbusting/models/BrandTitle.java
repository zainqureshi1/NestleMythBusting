package com.e2esp.nestlemythbusting.models;

/**
 * Created by Zain on 7/25/2017.
 */

public class BrandTitle {

    private String title;
    private int logoRes;

    public BrandTitle(String title, int logoRes) {
        this.title = title;
        this.logoRes = logoRes;
    }

    public String getTitle() {
        return title;
    }

    public int getLogoRes() {
        return logoRes;
    }

}
