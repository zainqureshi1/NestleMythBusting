package com.e2esp.nestlemythbusting.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;

import com.e2esp.nestlemythbusting.R;
import com.e2esp.nestlemythbusting.models.Video;
import com.e2esp.nestlemythbusting.utils.Consts;
import com.e2esp.nestlemythbusting.utils.Utility;

import java.io.File;

/**
 * Created by Zain on 7/11/2017.
 */

public class FileLoader {

    public static File getAppFolder(Context context) {
        try {
            File appFolder = new File(Environment.getExternalStorageDirectory(), context.getString(R.string.app_name));
            if (!appFolder.exists() || !appFolder.isDirectory()) {
                if (!appFolder.mkdir()) {
                    return null;
                }
            }
            return appFolder;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static File getBrandFolder(Context context, String brand) {
        try {
            File appFolder = getAppFolder(context);
            if (appFolder != null) {
                File brandFolder = new File(appFolder, brand);
                if (!brandFolder.exists() || !brandFolder.isDirectory()) {
                    if (!brandFolder.mkdir()) {
                        return null;
                    }
                }
                return brandFolder;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Video.Status getVideoStatus(Activity activity, String brand, String fileName) {
        Video.Status status = Video.Status.valueOf(Utility.Prefs
                .getPrefString(activity, fileName+ Consts.Keys.STATUS, Video.Status.NotDownloaded.name()));

        if (!PermissionManager.getInstance().hasPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            status = Video.Status.NotDownloaded;
        } else {
            File brandFolder = FileLoader.getBrandFolder(activity, brand);
            if (brandFolder != null) {
                File videoFile = new File(brandFolder, fileName);
                if (!videoFile.exists()) {
                    if (status != Video.Status.NotDownloaded) {
                        status = Video.Status.Deleted;
                    }
                } else {
                    if (status == Video.Status.Downloading) {
                        status = Video.Status.Incomplete;
                    } else if (status != Video.Status.Incomplete && status != Video.Status.Outdated) {
                        status = Video.Status.Downloaded;
                    }
                }
            } else if (status != Video.Status.NotDownloaded) {
                status = Video.Status.Deleted;
            }
        }

        return status;
    }

    public static File getFAQFile(Context context) {
        File appFolder = getAppFolder(context);
        if (appFolder != null) {
            File faqFile = new File(appFolder, "FAQ.txt");
            return faqFile;
        }
        return null;
    }

    public static File getTEMPFile(Context context) {
        File appFolder = getAppFolder(context);
        if (appFolder != null) {
            File faqFile = new File(appFolder, "TEMP.txt");
            return faqFile;
        }
        return null;
    }

}
