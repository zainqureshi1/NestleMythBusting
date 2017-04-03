package com.e2esp.nestlemythbusting.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.e2esp.nestlemythbusting.models.Video;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Zain on 3/24/2017.
 */

public class DownloadVideoTask extends AsyncTask<Void, Long, File> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final File mFile;
    private final Video mVideo;
    private final Callback mCallback;
    private Exception mException;

    public DownloadVideoTask(Context context, DbxClientV2 dbxClient, File file, Video video, Callback callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mFile = file;
        mVideo = video;
        mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mCallback.onDownloadStart(mVideo);
    }

    @Override
    protected File doInBackground(Void... params) {
        try {
            // Download the file.
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(mFile);
                DbxDownloader<FileMetadata> downloader =  mDbxClient.files().download(mVideo.getPath());

                // Track Progress
                long size = downloader.getResult().getSize();
                downloader.download(new ProgressOutputStream(size, outputStream, new ProgressOutputStream.Listener() {
                    @Override
                    public void progress(long completed, long totalSize) {
                        publishProgress(completed, totalSize);
                    }
                }));
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

            return mFile;
        } catch (Exception e) {
            mException = e;
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        super.onProgressUpdate(values);
        mCallback.onProgress(mVideo, values[0], values[1]);
    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mVideo, mException);
        } else {
            mCallback.onDownloadComplete(mVideo, result);
        }
    }

    public interface Callback {
        void onDownloadStart(Video video);
        void onProgress(Video video, long completed, long total);
        void onDownloadComplete(Video video, File result);
        void onError(Video video, Exception e);
    }

}
