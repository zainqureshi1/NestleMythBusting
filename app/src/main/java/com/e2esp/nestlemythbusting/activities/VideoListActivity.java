package com.e2esp.nestlemythbusting.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.e2esp.nestlemythbusting.R;
import com.e2esp.nestlemythbusting.adapters.VideoRecyclerAdapter;
import com.e2esp.nestlemythbusting.applications.NestleApplication;
import com.e2esp.nestlemythbusting.callbacks.OnVideoClickListener;
import com.e2esp.nestlemythbusting.models.Brand;
import com.e2esp.nestlemythbusting.models.Description;
import com.e2esp.nestlemythbusting.models.Video;
import com.e2esp.nestlemythbusting.utils.Consts;
import com.e2esp.nestlemythbusting.utils.DownloadVideoTask;
import com.e2esp.nestlemythbusting.utils.DownloadDescriptionTask;
import com.e2esp.nestlemythbusting.utils.DropboxClientFactory;
import com.e2esp.nestlemythbusting.utils.ListFolderTask;
import com.e2esp.nestlemythbusting.utils.PermissionManager;
import com.e2esp.nestlemythbusting.utils.Utility;
import com.e2esp.nestlemythbusting.utils.VerticalSpacingItemDecoration;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class VideoListActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getName();

    private TextView textViewSwipeHint;
    private SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView videosRecyclerView;
    private ArrayList<Video> videosArrayList;
    private VideoRecyclerAdapter videoRecyclerAdapter;

    private ListFolderTask listFolderTask;
    private ProgressDialog progressDialogList;

    private DownloadVideoTask downloadVideoTask;
    private ProgressDialog progressDialogDownload;

    private ArrayList<DownloadDescriptionTask> descriptionTasks;

    private Brand brand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        brand = getIntent().getParcelableExtra(Consts.Extras.BRAND);
        if (brand == null) {
            finish();
            return;
        }

        setContentView(R.layout.activity_video_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(brand.getName());

        setupView();
        startFilesLoading();
        sendAnalyticsScreenHit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.getInstance().onResult(requestCode, permissions, grantResults);
    }

    private void setupView() {
        textViewSwipeHint = (TextView) findViewById(R.id.textViewSwipeHint);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadVideoFiles();
            }
        });

        videosRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewVideos);
        videosArrayList = new ArrayList<>();
        videoRecyclerAdapter = new VideoRecyclerAdapter(this, videosArrayList, new OnVideoClickListener() {
            @Override
            public void onVideoClick(Video video) {
                videoClicked(video);
            }
            @Override
            public void onDownloadClick(Video video) {
                downloadClicked(video);
            }
        });
        videosRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        videosRecyclerView.addItemDecoration(new VerticalSpacingItemDecoration(Utility.dpToPx(this, 20)));
        videosRecyclerView.setAdapter(videoRecyclerAdapter);

        progressDialogList = new ProgressDialog(this);
        progressDialogList.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialogList.setCancelable(false);
        progressDialogList.setMessage("Loading Videos");

        progressDialogDownload = new ProgressDialog(this);
        progressDialogDownload.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialogDownload.setCancelable(false);
        progressDialogDownload.setMessage("Downloading Video");
        progressDialogDownload.setProgressNumberFormat("");
    }

    private void startFilesLoading() {
        PermissionManager.getInstance().checkPermissionRequest(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Consts.RequestCodes.PERMISSION_STORAGE,
                getString(R.string.app_name) + " require permission to save video in device storage",
                new PermissionManager.Callback() {
                    @Override
                    public void onGranted() {
                        loadVideoFiles();
                    }
                    @Override
                    public void onDenied() {
                        Utility.showToast(VideoListActivity.this, getString(R.string.cannot_download_video_permission_denied)
                                + " " + getString(R.string.grant_storage_permission));
                    }
                });
    }

    private void loadVideoFiles() {
        if (!Utility.isInternetConnected(this, true)) {
            swipeRefreshLayout.setRefreshing(false);
            progressDialogList.dismiss();
            textViewSwipeHint.setVisibility(View.VISIBLE);
            return;
        }

        swipeRefreshLayout.setRefreshing(true);
        textViewSwipeHint.setVisibility(View.GONE);
        if (videosArrayList.size() == 0) {
            progressDialogList.show();
        }

        if (listFolderTask != null) {
            listFolderTask.cancel(true);
        }

        listFolderTask = new ListFolderTask(DropboxClientFactory.getClient(), new ListFolderTask.Callback() {
            @Override
            public void onDataLoaded(ListFolderResult result) {
                swipeRefreshLayout.setRefreshing(false);
                progressDialogList.dismiss();
                videosArrayList.clear();
                if (result != null) {
                    List<Metadata> entities = result.getEntries();
                    if (entities != null) {
                        ArrayList<Description> descriptions = new ArrayList<>();
                        for (int i = 0; i < entities.size(); i++) {
                            Metadata entity = entities.get(i);
                            if (entity instanceof FileMetadata) {
                                if (isTextFile(entity.getName())) {
                                    descriptions.add(new Description(entity.getName(), entity.getPathLower()));
                                } else {
                                    Video.Status status = getVideoStatus(entity.getName());
                                    videosArrayList.add(new Video(entity.getName(), entity.getPathLower(), getVideoFile(entity.getName()).getAbsolutePath(), status));
                                }
                            }
                        }
                        for (int i = 0; i < descriptions.size(); i++) {
                            Description description = descriptions.get(i);
                            String fileName = description.getFileNameWithoutExt();
                            for (int j = 0; j < videosArrayList.size(); j++) {
                                if (fileName.equalsIgnoreCase(videosArrayList.get(j).getTitleWithoutExt())) {
                                    videosArrayList.get(j).setDescription(description);
                                    break;
                                }
                            }
                        }
                        Collections.sort(videosArrayList, new Video.Comparator());
                        videoRecyclerAdapter.notifyDataSetChanged();
                        updateVideosCount();
                        downloadDescriptions();
                        return;
                    }
                }

                Utility.showToast(VideoListActivity.this, getString(R.string.unable_to_get_videos));
                videoRecyclerAdapter.notifyDataSetChanged();
                textViewSwipeHint.setVisibility(View.VISIBLE);
            }
            @Override
            public void onError(Exception e) {
                swipeRefreshLayout.setRefreshing(false);
                progressDialogList.dismiss();
                textViewSwipeHint.setVisibility(View.VISIBLE);

                Log.e(TAG, "Failed to list folder.", e);
                Utility.showToast(VideoListActivity.this, getString(R.string.unable_to_get_videos));
            }
        });
        listFolderTask.execute(brand.getPath());
    }

    private boolean isTextFile(String fileName) {
        return fileName.toLowerCase(Locale.getDefault()).endsWith(".txt");
    }

    private Video.Status getVideoStatus(String fileName) {
        Video.Status status = Video.Status.valueOf(Utility.Prefs
                .getPrefString(this, fileName+Consts.Keys.STATUS, Video.Status.NotDownloaded.name()));

        if (!PermissionManager.getInstance().hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            status = Video.Status.NotDownloaded;
        } else if (status == Video.Status.Downloading) {
            status = Video.Status.Incomplete;
        } else if (status == Video.Status.Downloaded) {
            File brandFolder = getBrandFolder();
            if (brandFolder != null) {
                File videoFile = new File(brandFolder, fileName);
                if (!videoFile.exists()) {
                    status = Video.Status.Deleted;
                }
            } else {
                status = Video.Status.Deleted;
            }
        }

        return status;
    }

    private File getBrandFolder() {
        try {
            File appFolder = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
            if (!appFolder.exists() || !appFolder.isDirectory()) {
                if (!appFolder.mkdir()) {
                    return null;
                }
            }
            File brandFolder = new File(appFolder, brand.getName());
            if (!brandFolder.exists() || !brandFolder.isDirectory()) {
                if (!brandFolder.mkdir()) {
                    return null;
                }
            }
            return brandFolder;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private File getVideoFile(String videoName) {
        File brandFolder = getBrandFolder();
        if (brandFolder != null) {
            File videoFile = new File(brandFolder, videoName);
            return videoFile;
        }
        return null;
    }

    private void videoClicked(Video video) {
        int toastStringRes = -1;
        switch (video.getStatus()) {
            case Downloaded:
                Intent intent = new Intent(this, PlayerActivity.class);
                intent.putExtra(Consts.Extras.BRAND, brand);
                intent.putExtra(Consts.Extras.VIDEO, video);
                startActivity(intent);
                return;
            case NotDownloaded:
                toastStringRes = R.string.download_video_first;
                break;
            case Downloading:
                toastStringRes = R.string.wait_for_download_completion;
                break;
            case Incomplete:
            case Deleted:
                toastStringRes = R.string.download_video_again;
                break;
        }
        if (toastStringRes != -1) {
            Utility.showToast(this, toastStringRes);
        }
    }

    private void downloadClicked(final Video video) {
        PermissionManager.getInstance().checkPermissionRequest(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Consts.RequestCodes.PERMISSION_STORAGE,
                getString(R.string.app_name) + " require permission to save video in device storage",
                new PermissionManager.Callback() {
            @Override
            public void onGranted() {
                downloadVideo(video);
            }
            @Override
            public void onDenied() {
                Utility.showToast(VideoListActivity.this, getString(R.string.cannot_download_video_permission_denied)
                        + " " + getString(R.string.grant_storage_permission));
            }
        });
    }

    private void downloadVideo(Video videoToDownload) {
        progressDialogDownload.setProgress(0);
        progressDialogDownload.setMax(100);
        progressDialogDownload.show();

        if (listFolderTask != null) {
            listFolderTask.cancel(true);
        }

        File brandFolder = getVideoFile(videoToDownload.getTitle());
        downloadVideoTask = new DownloadVideoTask(this, DropboxClientFactory.getClient(), brandFolder, videoToDownload, new DownloadVideoTask.Callback() {
            @Override
            public void onDownloadStart(Video video) {
                updateVideoStatus(video, Video.Status.Downloading);
            }
            @Override
            public void onProgress(Video video, long completed, long total) {
                updateVideoProgress(video, completed, total);
            }
            @Override
            public void onDownloadComplete(Video video, File result) {
                updateVideoStatus(video, Video.Status.Downloaded);
                updateVideosCount();
            }
            @Override
            public void onError(Video video, Exception e) {
                if (video.getProgress() > 0) {
                    updateVideoStatus(video, Video.Status.Incomplete);
                } else {
                    updateVideoStatus(video, Video.Status.NotDownloaded);
                }
                Utility.showToast(VideoListActivity.this, "Error: "+e.getMessage());
            }
        });
        downloadVideoTask.execute();
    }

    private void updateVideoStatus(Video video, Video.Status status) {
        switch (status) {
            case Downloading:
                video.setProgress(0, "");
                break;
            case Downloaded:
            case Incomplete:
            case NotDownloaded:
                progressDialogDownload.dismiss();
                break;
        }
        Utility.Prefs.setPref(this, video.getTitle()+Consts.Keys.STATUS, status.name());
        video.setStatus(status);
        videoRecyclerAdapter.notifyItemChanged(videosArrayList.indexOf(video));
    }

    private void updateVideoProgress(Video video, long completed, long total) {
        int progress = (int)((float)(completed*100)/(float)total);
        String progressText = Utility.bytesString(completed)+" / "+Utility.bytesString(total);
        progressDialogDownload.setMax((int)total);
        progressDialogDownload.setProgress((int)completed);
        progressDialogDownload.setMessage(progressText);
        video.setProgress(progress, progressText);
        videoRecyclerAdapter.notifyItemChanged(videosArrayList.indexOf(video));
    }

    private void updateVideosCount() {
        int total = videosArrayList.size();
        int downloaded = 0;
        for (int i = 0; i < total; i++) {
            if (videosArrayList.get(i).getStatus() == Video.Status.Downloaded) {
                downloaded++;
            }
        }
        Utility.Prefs.setPref(VideoListActivity.this, brand.getName()+Consts.Keys.DOWNLOADED_VIDEOS, downloaded);
        Utility.Prefs.setPref(VideoListActivity.this, brand.getName()+Consts.Keys.TOTAL_VIDEOS, total);
    }

    private int descriptionsToDownload;
    private void downloadDescriptions() {
        cancelDescriptionTasks();
        descriptionTasks = new ArrayList<>();
        descriptionsToDownload = videosArrayList.size();
        for (int i = 0; i < videosArrayList.size(); i++) {
            Description description = videosArrayList.get(i).getDescription();
            if (description != null) {
                DownloadDescriptionTask downloadDescriptionTask = new DownloadDescriptionTask(DropboxClientFactory.getClient(), description, new DownloadDescriptionTask.Callback() {
                    public void onDownloadComplete(Description description, String result) {
                        description.setDescription(result);
                        descriptionsToDownload--;
                        if (descriptionsToDownload <= 0) {
                            videoRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                    public void onError(Description description, Exception e) {
                        descriptionsToDownload--;
                        if (descriptionsToDownload <= 0) {
                            videoRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                });
                downloadDescriptionTask.execute();
                descriptionTasks.add(downloadDescriptionTask);
            }
        }
    }

    private void sendAnalyticsScreenHit() {
        Tracker tracker = ((NestleApplication)getApplication()).getTracker();
        tracker.setScreenName(brand.getName()+ " Video List Screen");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
        tracker.setScreenName(null);
    }

    private void cancelDescriptionTasks() {
        if (descriptionTasks != null) {
            for (int i = 0; i < descriptionTasks.size(); i++) {
                descriptionTasks.get(i).cancel(true);
            }
            descriptionTasks.clear();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (listFolderTask != null) {
            listFolderTask.cancel(true);
        }
        if (downloadVideoTask != null) {
            downloadVideoTask.cancel(true);
        }
        cancelDescriptionTasks();
    }

}
