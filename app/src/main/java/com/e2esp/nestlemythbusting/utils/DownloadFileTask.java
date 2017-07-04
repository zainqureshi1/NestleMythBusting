package com.e2esp.nestlemythbusting.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.v2.DbxClientV2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Zain on 5/31/2017.
 */

public class DownloadFileTask extends AsyncTask<Void, Void, File> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final File mFile;
    private final String mPath;
    private final Callback mCallback;
    private Exception mException;

    public DownloadFileTask(Context context, DbxClientV2 dbxClient, File file, String path, Callback callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mFile = file;
        mPath = path;
        mCallback = callback;
    }

    @Override
    protected File doInBackground(Void... params) {
        try {
            // Download the file.
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(mFile);
                mDbxClient.files().download(mPath).download(outputStream);
            } catch (Exception e) {
                mException = e;
                return null;
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            mException = e;
        }

        return mFile;
    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDownloadComplete(result);
        }
    }

    public interface Callback {
        void onDownloadComplete(File result);
        void onError(Exception e);
    }

}
