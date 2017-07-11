package com.e2esp.nestlemythbusting.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.e2esp.nestlemythbusting.models.Description;
import com.e2esp.nestlemythbusting.models.Video;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Zain on 3/24/2017.
 */

public class DownloadDescriptionTask extends AsyncTask<Void, Long, String> {

    private final DbxClientV2 mDbxClient;
    private final Description mDescription;
    private final Callback mCallback;
    private Exception mException;

    public DownloadDescriptionTask(DbxClientV2 dbxClient, Description description, Callback callback) {
        mDbxClient = dbxClient;
        mDescription = description;
        mCallback = callback;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            DbxDownloader<FileMetadata> downloader =  mDbxClient.files().download(mDescription.getFilePath());
            /*URL url = new URL(downloader.get);
            HttpURLConnection.setFollowRedirects(true);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(false);
            con.setReadTimeout(20000);
            con.setRequestProperty("Connection", "keep-alive");

            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:16.0) Gecko/20100101 Firefox/16.0");
            ((HttpURLConnection) con).setRequestMethod("GET");
            con.setConnectTimeout(5000);*/
            BufferedInputStream in = new BufferedInputStream(downloader.getInputStream());
            /*int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println(responseCode);
            }*/
            StringBuffer buffer = new StringBuffer();
            int charsRead;
            while ((charsRead = in.read()) != -1)
            {
                char g = (char) charsRead;
                buffer.append(g);
            }
            String desc = buffer.toString();
            return desc;
        } catch (Exception e) {
            mException = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mDescription, mException);
        } else {
            mCallback.onDownloadComplete(mDescription, result);
        }
    }

    public interface Callback {
        void onDownloadComplete(Description description, String result);
        void onError(Description description, Exception e);
    }

}
