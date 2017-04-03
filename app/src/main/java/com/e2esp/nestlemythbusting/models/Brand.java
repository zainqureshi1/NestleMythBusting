package com.e2esp.nestlemythbusting.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

/**
 * Created by Zain on 3/22/2017.
 */

public class Brand implements Parcelable {

    private String name;
    private String path;
    private String logoPath;

    private int totalVideos;
    private int downloadedVideos;

    public Brand(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setVideos(int totalVideos, int downloadedVideos) {
        this.totalVideos = totalVideos;
        this.downloadedVideos = downloadedVideos;
    }

    public int getTotalVideos() {
        return totalVideos;
    }

    public int getDownloadedVideos() {
        return downloadedVideos;
    }

    public Brand(Parcel in) {
        this.name = in.readString();
        this.path = in.readString();
        this.logoPath = in.readString();
        this.totalVideos = in.readInt();
        this.downloadedVideos = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getName());
        dest.writeString(getPath());
        dest.writeString(getLogoPath());
        dest.writeInt(getTotalVideos());
        dest.writeInt(getDownloadedVideos());
    }

    public static final Parcelable.Creator<Brand> CREATOR = new Parcelable.Creator<Brand>() {
        public Brand createFromParcel(Parcel p) {
            Brand brand = new Brand(p);
            if (brand == null) {
                throw new RuntimeException("Failed to unparcel Brand");
            }
            return brand;
        }
        public Brand[] newArray(int size) {
            return new Brand[size];
        }
    };

    public static class Comparator implements java.util.Comparator<Brand> {
        @Override
        public int compare(Brand brand1, Brand brand2) {
            if (isCorporateBrand(brand1)) {
                return 1;
            }
            if (isCorporateBrand(brand2)) {
                return -1;
            }
            return brand1.getName().compareTo(brand2.getName());
        }
        private boolean isCorporateBrand(Brand brand) {
            return brand.getName().toLowerCase(Locale.getDefault()).contains("corporate");
        }
    }

}
