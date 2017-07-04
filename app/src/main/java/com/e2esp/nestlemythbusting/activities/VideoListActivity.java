package com.e2esp.nestlemythbusting.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.e2esp.nestlemythbusting.models.Brand;
import com.e2esp.nestlemythbusting.models.Description;
import com.e2esp.nestlemythbusting.models.Title;
import com.e2esp.nestlemythbusting.models.Video;
import com.e2esp.nestlemythbusting.utils.Consts;
import com.e2esp.nestlemythbusting.utils.DownloadFileTask;
import com.e2esp.nestlemythbusting.utils.DownloadVideoTask;
import com.e2esp.nestlemythbusting.utils.DownloadDescriptionTask;
import com.e2esp.nestlemythbusting.utils.DropboxClientFactory;
import com.e2esp.nestlemythbusting.utils.ListFolderTask;
import com.e2esp.nestlemythbusting.utils.OfflineDataLoader;
import com.e2esp.nestlemythbusting.utils.PermissionManager;
import com.e2esp.nestlemythbusting.utils.UploadFileTask;
import com.e2esp.nestlemythbusting.utils.Utility;
import com.e2esp.nestlemythbusting.utils.VerticalSpacingItemDecoration;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VideoListActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getName();

    private TextView textViewSwipeHint;
    private SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView videosRecyclerView;
    private ArrayList<Video> videosArrayList;
    private VideoRecyclerAdapter videoRecyclerAdapter;

    private AppCompatEditText editTextFAQ;
    private Button buttonSubmit;

    private ListFolderTask listFolderTask;
    private ProgressDialog progressDialogList;

    private DownloadVideoTask downloadVideoTask;
    private ProgressDialog progressDialogDownload;

    private ArrayList<DownloadDescriptionTask> descriptionTasks;
    private DownloadFileTask downloadFAQTask;
    private UploadFileTask uploadFAQTask;
    private ProgressDialog progressDialogSubmitQuestion;

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

            @Override
            public void onLongClick(Video video) {
                videoLongClicked(video);
            }
        });
        videosRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        videosRecyclerView.addItemDecoration(new VerticalSpacingItemDecoration(Utility.dpToPx(this, 20)));
        videosRecyclerView.setAdapter(videoRecyclerAdapter);

        editTextFAQ = (AppCompatEditText) findViewById(R.id.editTextFAQ);
        buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
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

        progressDialogSubmitQuestion = new ProgressDialog(this);
        progressDialogSubmitQuestion.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialogSubmitQuestion.setCancelable(false);
        progressDialogSubmitQuestion.setMessage("Submitting Question");
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

        ArrayList<Title> titlesList = OfflineDataLoader.videoTitlesList(brand.getName());
        for (Title title: titlesList) {
            videosArrayList.add(createOfflineVideo(title.getTitle(), title.getDescription()));
        }

        Collections.sort(videosArrayList, new Video.Comparator());
        videoRecyclerAdapter.notifyDataSetChanged();
        updateVideosCount();
        checkForUpdates();
    }

    private Video createOfflineVideo(String videoName, String description) {
        String brandPath = "/" + brand.getName().toLowerCase() + "/";
        return new Video(videoName+".mp4", brandPath+videoName.toLowerCase()+".mp4", getVideoFile(videoName+".mp4").getAbsolutePath(), getVideoStatus(videoName+".mp4"), new Description(videoName+".txt", brandPath+videoName.toLowerCase()+".txt", description));
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

    private Video.Status getVideoStatus(String fileName) {
        Video.Status status = Video.Status.valueOf(Utility.Prefs
                .getPrefString(this, fileName+Consts.Keys.STATUS, Video.Status.NotDownloaded.name()));

        if (!PermissionManager.getInstance().hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            status = Video.Status.NotDownloaded;
        } else {
            File brandFolder = getBrandFolder();
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

        ListFolderTask listFolderForUpdatesTask = new ListFolderTask(DropboxClientFactory.getClient(), new ListFolderTask.Callback() {
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

    private File getAppFolder() {
        try {
            File appFolder = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
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

    private File getBrandFolder() {
        try {
            File appFolder = getAppFolder();
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

    private File getVideoFile(String videoName) {
        File brandFolder = getBrandFolder();
        if (brandFolder != null) {
            File videoFile = new File(brandFolder, videoName);
            return videoFile;
        }
        return null;
    }

    private File getFAQFile() {
        File appFolder = getAppFolder();
        if (appFolder != null) {
            File faqFile = new File(appFolder, "FAQ.txt");
            return faqFile;
        }
        return null;
    }

    private void videoClicked(Video video) {
        int toastStringRes = -1;
        switch (video.getStatus()) {
            case Downloaded:
            case Outdated:
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
        downloadVideoTask = new DownloadVideoTask(this, DropboxClientFactory.getClient(), videoFile, videoToDownload, new DownloadVideoTask.Callback() {
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
            Video.Status status = videosArrayList.get(i).getStatus();
            if (status == Video.Status.Downloaded || status == Video.Status.Outdated) {
                downloaded++;
            }
        }
        Utility.Prefs.setPref(VideoListActivity.this, brand.getName()+Consts.Keys.DOWNLOADED_VIDEOS, downloaded);
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

    private void onSubmitFAQClicked() {
        String question = editTextFAQ.getText().toString();
        if (question.isEmpty()) {
            Utility.showToast(this, R.string.type_question_first);
            editTextFAQ.requestFocus();
            return;
        }
        Utility.hideKeyboard(this, editTextFAQ);

        startFAQLoading();
    }

    private void startFAQLoading() {
        PermissionManager.getInstance().checkPermissionRequest(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Consts.RequestCodes.PERMISSION_STORAGE,
                getString(R.string.app_name) + " require permission to save questions in device storage",
                new PermissionManager.Callback() {
                    @Override
                    public void onGranted() {
                        downloadFAQ();
                    }
                    @Override
                    public void onDenied() {
                        Utility.showToast(VideoListActivity.this, getString(R.string.cannot_download_questions_permission_denied)
                                + " " + getString(R.string.grant_storage_permission));
                    }
                });
    }

    private void downloadFAQ() {
        if (!Utility.isInternetConnected(this, true)) {
            return;
        }

        progressDialogSubmitQuestion.setMessage("Loading FAQ");
        progressDialogSubmitQuestion.show();

        if (downloadFAQTask != null) {
            downloadFAQTask.cancel(true);
        }

        File faqFile = getFAQFile();
        downloadFAQTask = new DownloadFileTask(this, DropboxClientFactory.getClient(), faqFile, Consts.FAQFilePath, new DownloadFileTask.Callback() {
            @Override
            public void onDownloadComplete(File result) {
                addQuestionInFAQFile(result);
            }
            @Override
            public void onError(Exception e) {
                progressDialogSubmitQuestion.dismiss();
                Utility.showToast(VideoListActivity.this, "Error: "+e.getMessage());
            }
        });
        downloadFAQTask.execute();
    }

    private void addQuestionInFAQFile(File file) {
        progressDialogSubmitQuestion.setMessage("Adding Question in FAQ");
        progressDialogSubmitQuestion.show();

        String faq = readFAQFile(file);
        String brandMarker = ">-BRAND: ";
        String questionMarker = "-QUESTION: ";
        String dateTime = currentDateTimeString();
        String selectedBrandName = brand.getName();
        String newQuestion = editTextFAQ.getText().toString();
        String selectedBrand = selectedBrandName;

        // split selected brand questions from all questions
        int brandIndex = -1;
        String[] brands = faq.split(brandMarker);
        for (int i = 0; i < brands.length; i++) {
            if (brands[i].startsWith(selectedBrandName)) {
                selectedBrand = brands[i];
                brandIndex = i;
                break;
            }
        }

        // remove previous last update time and add new time
        int index1 = selectedBrand.indexOf('(');
        int index2 = selectedBrand.indexOf(')');
        String newLastUpdate = "(last update: "+dateTime+")";
        if (index1 >= 0 && index2 > 0 & index1 < index2) {
            String toRemove = selectedBrand.substring(index1, index2+1);
            selectedBrand = selectedBrand.replace(toRemove, newLastUpdate);
        } else {
            String lastSegment = (selectedBrand.length() > selectedBrandName.length()+1) ? selectedBrand.substring(selectedBrandName.length()+1) : "";
            selectedBrand = selectedBrandName + " " + newLastUpdate + lastSegment;
        }

        // add new question and rejoin all questions
        selectedBrand += "\n" + questionMarker + newQuestion;
        selectedBrand += " <" + dateTime + ">";
        if (brandIndex >= 0) {
            brands[brandIndex] = selectedBrand;
        }
        String newFAQ = "";
        for (int i = 0; i < brands.length; i++) {
            if (i > 0) {
                newFAQ += brandMarker;
            }
            newFAQ += brands[i];
        }
        if (brandIndex < 0) {
            if (!newFAQ.isEmpty()) {
                newFAQ += "\n\n\n";
            }
            newFAQ += brandMarker + selectedBrand;
        }

        // write questions to file
        if (writeToFAQFile(newFAQ, file)) {
            // upload file
            uploadFAQ(file);
        }
    }

    private String readFAQFile(File file) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append('\n');
            }
            bufferedReader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private boolean writeToFAQFile(String faq, File file) {
        FileOutputStream fileOutputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(faq);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (outputStreamWriter != null) {
                    outputStreamWriter.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void uploadFAQ(File file) {
        progressDialogSubmitQuestion.setMessage("Uploading Updated FAQ");
        progressDialogSubmitQuestion.show();

        if (uploadFAQTask != null) {
            uploadFAQTask.cancel(true);
        }
        uploadFAQTask = new UploadFileTask(this, DropboxClientFactory.getClient(), file, Consts.FAQFilePath, new UploadFileTask.Callback() {
            @Override
            public void onUploadComplete(FileMetadata result) {
                progressDialogSubmitQuestion.dismiss();
                Utility.showToast(VideoListActivity.this, "Successfully submitted question");
                editTextFAQ.setText("");
            }
            @Override
            public void onError(Exception e) {
                progressDialogSubmitQuestion.dismiss();
                Utility.showToast(VideoListActivity.this, "Error: "+e.getMessage());
            }
        });
        uploadFAQTask.execute();
    }

    private String currentDateTimeString() {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mma");
            return simpleDateFormat.format(new Date());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
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
