package com.e2esp.nestlemythbusting.models;

/**
 * Created by Zain on 4/24/2017.
 */

public class Title {

    private String title;
    private String description;

    public Title(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

}