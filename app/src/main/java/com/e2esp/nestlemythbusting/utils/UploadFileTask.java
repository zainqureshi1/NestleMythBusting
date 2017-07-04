package com.e2esp.nestlemythbusting.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Zain on 5/31/2017.
 */

public class UploadFileTask extends AsyncTask<Void, Void, FileMetadata> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final File mFile;
    private final String mPath;
    private final Callback mCallback;
    private Exception mException;

    public UploadFileTask(Context context, DbxClientV2 dbxClient, File file, String path, Callback callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mFile = file;
        mPath = path;
        mCallback = callback;
    }

    @Override
    protected FileMetadata doInBackground(Void... params) {
            try {
                InputStream inputStream = new FileInputStream(mFile);
                return mDbxClient.files().uploadBuilder(mPath)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(inputStream);
            } catch (DbxException | IOException e) {
                mException = e;
            }
        return null;
    }

    @Override
    protected void onPostExecute(FileMetadata result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else if (result == null) {
            mCallback.onError(null);
        } else {
            mCallback.onUploadComplete(result);
        }
    }

    public interface Callback {
        void onUploadComplete(FileMetadata result);
        void onError(Exception e);
    }

}
