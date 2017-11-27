package com.e2esp.nestlemythbusting.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.e2esp.nestlemythbusting.R;
import com.e2esp.nestlemythbusting.adapters.VideoRecyclerAdapter;
import com.e2esp.nestlemythbusting.applications.NestleApplication;
import com.e2esp.nestlemythbusting.callbacks.OnVideoClickListener;
import com.e2esp.nestlemythbusting.helpers.FAQHandler;
import com.e2esp.nestlemythbusting.helpers.FileLoader;
import com.e2esp.nestlemythbusting.models.Brand;
import com.e2esp.nestlemythbusting.models.Description;
import com.e2esp.nestlemythbusting.models.VideoTitle;
import com.e2esp.nestlemythbusting.models.Video;
import com.e2esp.nestlemythbusting.utils.Consts;
import com.e2esp.nestlemythbusting.tasks.DownloadVideoTask;
import com.e2esp.nestlemythbusting.tasks.DownloadDescriptionTask;
import com.e2esp.nestlemythbusting.helpers.DropboxClientFactory;
import com.e2esp.nestlemythbusting.tasks.ListFolderTask;
import com.e2esp.nestlemythbusting.helpers.OfflineDataLoader;
import com.e2esp.nestlemythbusting.helpers.PermissionManager;
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
    private final int REQUEST_CODE_PLAYER = 1021;

    private TextView textViewSwipeHint;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<Video> videosArrayList;
    private VideoRecyclerAdapter videoRecyclerAdapter;

    private AppCompatEditText editTextFAQ;

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
        startOfflineLoading();
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
                startFilesLoading();
            }
        });
        swipeRefreshLayout.setEnabled(false);

        RecyclerView videosRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewVideos);
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

            @Override
            public void onLongClick(Video video) {
                videoLongClicked(video);
            }
        });
        videosRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        videosRecyclerView.addItemDecoration(new VerticalSpacingItemDecoration(Utility.dpToPx(this, 20)));
        videosRecyclerView.setAdapter(videoRecyclerAdapter);

        editTextFAQ = (AppCompatEditText) findViewById(R.id.editTextFAQ);
        Button buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmitFAQClicked();
            }
        });

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

    private void startOfflineLoading() {
        PermissionManager.getInstance().checkPermissionRequest(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Consts.RequestCodes.PERMISSION_STORAGE,
                getString(R.string.app_name) + " require permission to save video in device storage",
                new PermissionManager.Callback() {
                    @Override
                    public void onGranted() {
                        loadVideoTitlesOffline();
                    }
                    @Override
                    public void onDenied() {
                        Utility.showToast(VideoListActivity.this, getString(R.string.cannot_download_video_permission_denied)
                                + " " + getString(R.string.grant_storage_permission));
                    }
                });
    }

    private void loadVideoTitlesOffline() {
        textViewSwipeHint.setVisibility(View.GONE);
        videosArrayList.clear();

        ArrayList<VideoTitle> titlesList = OfflineDataLoader.videoTitlesList(brand.getName());
        for (VideoTitle videoTitle : titlesList) {
            videosArrayList.add(createOfflineVideo(videoTitle.getTitle(), videoTitle.getDescription()));
        }

        Collections.sort(videosArrayList, new Video.Comparator());
        videoRecyclerAdapter.notifyDataSetChanged();
        checkForUpdates();
    }

    private Video createOfflineVideo(String videoName, String description) {
        String brandPath = "/" + brand.getName().toLowerCase() + "/";
        String path = brandPath+videoName.toLowerCase();
        String filePath = getVideoFile(videoName+".mp4").getAbsolutePath();
        Video.Status status = FileLoader.getVideoStatus(this, brand.getName(), videoName+".mp4");
        Description videoDescription = new Description(videoName+".txt", path+".txt", description);
        return new Video(videoName+".mp4", path+".mp4", filePath, status, videoDescription);
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

        listFolderTask = new ListFolderTask(DropboxClientFactory.getClient(this), new ListFolderTask.Callback() {
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
                                    Video.Status status = FileLoader.getVideoStatus(VideoListActivity.this, brand.getName(), entity.getName());
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
                        downloadDescriptions();
                        checkForUpdates();
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

    private void checkForUpdates() {
        if (!PermissionManager.getInstance().hasPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            return;
        }
        if (!Utility.isInternetConnected(this, false)) {
            return;
        }

        boolean hasDownloads = false;
        for (int i = 0; i < videosArrayList.size(); i++) {
            Video.Status status = videosArrayList.get(i).getStatus();
            if (status == Video.Status.Downloaded || status == Video.Status.Incomplete) {
                hasDownloads = true;
                break;
            }
        }
        if (!hasDownloads) {
            return;
        }

        ListFolderTask listFolderForUpdatesTask = new ListFolderTask(DropboxClientFactory.getClient(this), new ListFolderTask.Callback() {
            @Override
            public void onDataLoaded(ListFolderResult result) {
                if (result != null) {
                    List<Metadata> entities = result.getEntries();
                    if (entities != null) {
                        boolean needsUpdate = false;
                        for (int i = 0; i < entities.size(); i++) {
                            Metadata entity = entities.get(i);
                            if (entity instanceof FileMetadata) {
                                if (!isTextFile(entity.getName())) {
                                    if (checkIfNeedsUpdate(entity.getName(), ((FileMetadata) entity).getSize())) {
                                        needsUpdate = true;
                                    }
                                }
                            }
                        }
                        if (needsUpdate) {
                            videoRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to list folder for updates.", e);
            }
        });
        listFolderForUpdatesTask.execute(brand.getPath());
    }

    private boolean checkIfNeedsUpdate(String videoName, long updatedSize) {
        for (int i = 0; i < videosArrayList.size(); i++) {
            Video video = videosArrayList.get(i);
            if (video.getTitle().equals(videoName)) {
                if (video.getStatus() == Video.Status.Downloaded || video.getStatus() == Video.Status.Incomplete) {
                    File videoFile = getVideoFile(video.getTitle());
                    long fileLength = videoFile.length();
                    if (video.getStatus() == Video.Status.Downloaded) {
                        if (fileLength != updatedSize) {
                            updateVideoStatus(video, Video.Status.Outdated);
                            return true;
                        }
                    } else {
                        if (fileLength > updatedSize) {
                            updateVideoStatus(video, Video.Status.Outdated);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private File getVideoFile(String videoName) {
        File brandFolder = FileLoader.getBrandFolder(this, brand.getName());
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
                startActivityForResult(intent, REQUEST_CODE_PLAYER);
                return;
            case NotDownloaded:
                toastStringRes = R.string.download_video_first;
                break;
            case Downloading:
                toastStringRes = R.string.wait_for_download_completion;
                break;
            case Incomplete:
            case Deleted:
            case Outdated:
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

    private void videoLongClicked(final Video video) {
        CharSequence[] buttonTitles;
        Video.Status status = video.getStatus();
        if (status != Video.Status.NotDownloaded && status != Video.Status.Deleted) {
            buttonTitles = new CharSequence[]{getString(R.string.delete), getString(R.string.download_again), getString(R.string.cancel)};
        } else {
            buttonTitles = new CharSequence[]{getString(R.string.download), getString(R.string.cancel)};
        }
        final int buttonCount = buttonTitles.length;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(video.getTitleWithoutExt());
        builder.setItems(buttonTitles,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        which += (3 - buttonCount);
                        switch (which) {
                            case 0:
                                try {
                                    getVideoFile(video.getTitle()).delete();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                updateVideoStatus(video, Video.Status.Deleted);
                                videoRecyclerAdapter.notifyDataSetChanged();
                                break;
                            case 1:
                                downloadVideo(video);
                                break;
                            case 2:
                                break;
                        }
                    }
                });
        builder.create().show();
    }

    private void downloadVideo(Video videoToDownload) {
        if (!Utility.isInternetConnected(this, true)) {
            return;
        }

        progressDialogDownload.setProgress(0);
        progressDialogDownload.setMax(100);
        progressDialogDownload.show();

        if (downloadVideoTask != null) {
            downloadVideoTask.cancel(true);
        }

        File videoFile = getVideoFile(videoToDownload.getTitle());
        downloadVideoTask = new DownloadVideoTask(DropboxClientFactory.getClient(this), videoFile, videoToDownload, new DownloadVideoTask.Callback() {
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

    private int descriptionsToDownload;
    private void downloadDescriptions() {
        cancelDescriptionTasks();
        descriptionTasks = new ArrayList<>();
        descriptionsToDownload = videosArrayList.size();
        for (int i = 0; i < videosArrayList.size(); i++) {
            Description description = videosArrayList.get(i).getDescription();
            if (description != null) {
                DownloadDescriptionTask downloadDescriptionTask = new DownloadDescriptionTask(DropboxClientFactory.getClient(this), description, new DownloadDescriptionTask.Callback() {
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

    private void onSubmitFAQClicked() {
        String question = editTextFAQ.getText().toString();
        if (question.isEmpty()) {
            Utility.showToast(this, R.string.type_question_first);
            editTextFAQ.requestFocus();
            return;
        }
        Utility.hideKeyboard(this, editTextFAQ);

        startSavingFAQ();
    }

    private void startSavingFAQ() {
        PermissionManager.getInstance().checkPermissionRequest(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Consts.RequestCodes.PERMISSION_STORAGE,
                getString(R.string.app_name) + " require permission to save questions in device storage",
                new PermissionManager.Callback() {
                    @Override
                    public void onGranted() {
                        FAQHandler.getInstance(VideoListActivity.this).saveFAQ(brand, editTextFAQ.getText().toString());
                        editTextFAQ.setText("");
                    }
                    @Override
                    public void onDenied() {
                        Utility.showToast(VideoListActivity.this, getString(R.string.cannot_download_questions_permission_denied)
                                + " " + getString(R.string.grant_storage_permission));
                    }
                });
    }

    private void showVideoErrorDialog(String title) {
        Utility.showOkAlert(this, getString(R.string.unable_to_play_video, title), getString(R.string.unable_to_play_video_explanation), null, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PLAYER && resultCode == RESULT_FIRST_USER) {
            if (data != null) {
                Video returnedVideo = data.getParcelableExtra(Consts.Extras.VIDEO);
                if (returnedVideo != null) {
                    String videoTitle = returnedVideo.getTitle();
                    for (Video video : videosArrayList) {
                        if (video.getTitle().equals(videoTitle)) {
                            updateVideoStatus(video, Video.Status.NotPlayable);
                            showVideoErrorDialog(videoTitle);
                            break;
                        }
                    }
                }
            }
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
