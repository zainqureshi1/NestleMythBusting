package com.e2esp.nestlemythbusting.models;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.e2esp.nestlemythbusting.utils.Utility;

/**
 * Created by Zain on 3/22/2017.
 */

public class Video implements Parcelable {

    public enum Status {
        NotDownloaded,
        Downloading,
        Downloaded,
        Incomplete,
        Outdated,
        NotPlayable,
        Deleted
    }

    private String title;
    private String path;
    private Description description;
    private String filePath;
    private Bitmap thumbnail;

    private Status status;
    private int progress;
    private String progressText;

    public Video(String title, String path, String filePath, Status status) {
        this.title = title;
        this.path = path;
        this.filePath = filePath;
        this.status = status;
    }

    public Video(String title, String path, String filePath, Status status, Description description) {
        this.title = title;
        this.path = path;
        this.filePath = filePath;
        this.status = status;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleWithoutExt() {
        return title.substring(0, title.lastIndexOf("."));
    }

    public String getPath() {
        return path;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public Description getDescription() {
        return description;
    }

    public String getFilePath() {
        return filePath;
    }

    public Bitmap getThumbnail() {
        if (thumbnail == null && filePath != null) {
            thumbnail = Utility.createVideoThumbnail(filePath, 4);
        }
        return thumbnail;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public void setProgress(int progress, String progressText) {
        this.progress = progress;
        this.progressText = progressText;
    }

    public int getProgress() {
        return progress;
    }

    public String getProgressText() {
        return progressText;
    }

    public Video(Parcel in) {
        this.title = in.readString();
        this.filePath = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getTitle());
        dest.writeString(getFilePath());
    }

    public static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>() {
        public Video createFromParcel(Parcel p) {
            Video video = new Video(p);
            if (video == null) {
                throw new RuntimeException("Failed to unparcel Video");
            }
            return video;
        }
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    public static class Comparator implements java.util.Comparator<Video> {
        @Override
        public int compare(Video video1, Video video2) {
            return video1.getTitle().compareTo(video2.getTitle());
        }
    }

}
