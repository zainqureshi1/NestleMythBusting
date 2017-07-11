package com.e2esp.nestlemythbusting.helpers;

import android.content.Context;
import android.os.Environment;

import com.e2esp.nestlemythbusting.R;
import com.e2esp.nestlemythbusting.models.Brand;

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

    public static File getBrandFolder(Context context, Brand brand) {
        try {
            File appFolder = getAppFolder(context);
            if (appFolder != null) {
                File brandFolder = new File(appFolder, brand.getName());
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
