package com.e2esp.nestlemythbusting.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.e2esp.nestlemythbusting.helpers.FileLoader;
import com.e2esp.nestlemythbusting.models.BrandVideosToDownload;
import com.e2esp.nestlemythbusting.models.VideoToDownload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by Zain on 3/24/2017.
 */

public class DownloadMultipleVideosTask extends AsyncTask<Void, Void, Integer[]> {
    private final String TAG = this.getClass().getSimpleName();

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final ArrayList<BrandVideosToDownload> mBrandVideosToDownloadList;
    private final Callback mCallback;

    public DownloadMultipleVideosTask(Context context, DbxClientV2 dbxClient, ArrayList<BrandVideosToDownload> brandVideosList, Callback callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mBrandVideosToDownloadList = brandVideosList;
        mCallback = callback;
    }

    @Override
    protected Integer[] doInBackground(Void... params) {
        int downloaded = 0;
        int total = 0;
        for (int i = 0; i < mBrandVideosToDownloadList.size(); i++) {
            if (isCancelled()) {
                break;
            }
            BrandVideosToDownload brandVideos = mBrandVideosToDownloadList.get(i);
            Log.d(TAG, "Downloading videos of brand "+brandVideos.getBrand());
            try {
                File brandFolder = FileLoader.getBrandFolder(mContext, brandVideos.getBrand());
                if (brandFolder == null) {
                    Log.e(TAG, "Unable to create brand folder");
                    continue;
                }

                ArrayList<VideoToDownload> videosToDownload = brandVideos.getVideosToDownload();
                total += videosToDownload.size();

                for (int j = 0; j < videosToDownload.size(); j++) {
                    if (isCancelled()) {
                        break;
                    }
                    VideoToDownload video = videosToDownload.get(j);
                    Log.d(TAG, "Downloading video at "+video.getPath());
                    File videoFile = new File(brandFolder, video.getTitle());

                    // Download the file.
                    OutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(videoFile);
                        DbxDownloader<FileMetadata> downloader = mDbxClient.files().download(video.getPath());
                        downloader.download(outputStream);
                        downloaded += 1;
                        Log.i(TAG, "Downloaded video at "+video.getPath());
                    } catch (Exception e) {
                        Log.e(TAG, "Error while downloading video at "+video.getPath());
                        e.printStackTrace();
                    } finally {
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error while downloading videos of brand "+brandVideos.getBrand());
                e.printStackTrace();
            }
        }

        return new Integer[]{downloaded, total};
    }

    @Override
    protected void onPostExecute(Integer[] result) {
        super.onPostExecute(result);
        if (result != null && result.length > 1) {
            mCallback.onDownloadComplete(result[0], result[1]);
        }
    }

    public interface Callback {
        void onDownloadComplete(int downloaded, int total);
    }

}
