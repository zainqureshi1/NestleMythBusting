package com.e2esp.nestlemythbusting.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.TextView;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.e2esp.nestlemythbusting.R;
import com.e2esp.nestlemythbusting.adapters.BrandRecyclerAdapter;
import com.e2esp.nestlemythbusting.callbacks.OnBrandClickListener;
import com.e2esp.nestlemythbusting.helpers.FileLoader;
import com.e2esp.nestlemythbusting.models.Brand;
import com.e2esp.nestlemythbusting.models.BrandTitle;
import com.e2esp.nestlemythbusting.models.BrandVideosToDownload;
import com.e2esp.nestlemythbusting.models.Video;
import com.e2esp.nestlemythbusting.models.VideoTitle;
import com.e2esp.nestlemythbusting.models.VideoToDownload;
import com.e2esp.nestlemythbusting.tasks.DownloadMultipleVideosTask;
import com.e2esp.nestlemythbusting.utils.Consts;
import com.e2esp.nestlemythbusting.helpers.DropboxClientFactory;
import com.e2esp.nestlemythbusting.tasks.ListFolderTask;
import com.e2esp.nestlemythbusting.helpers.OfflineDataLoader;
import com.e2esp.nestlemythbusting.helpers.PicassoClient;
import com.e2esp.nestlemythbusting.utils.Utility;
import com.e2esp.nestlemythbusting.utils.VerticalSpacingItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Zain on 3/22/2017.
 */

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getName();

    private TextView textViewSwipeHint;
    private TextView textViewDescription;
    private SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView brandRecyclerView;
    private ArrayList<Brand> brandArrayList;
    private BrandRecyclerAdapter brandRecyclerAdapter;

    private ListFolderTask listFolderTask;
    private ProgressDialog progressDialog;

    private ArrayList<BrandVideosToDownload> brandVideosToDownload;
    private DownloadMultipleVideosTask downloadMultipleVideosTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_left);

        setupDropbox();
        setupView();
        loadBrandFoldersOffline();
    }

    private void setupView() {
        Typeface font = Utility.getArialFont(this);

        textViewSwipeHint = (TextView) findViewById(R.id.textViewSwipeHint);
        textViewSwipeHint.setTypeface(font);
        textViewDescription = (TextView) findViewById(R.id.textViewBrandsDescription);
        textViewDescription.setTypeface(font);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadBrandFolders();
            }
        });
        swipeRefreshLayout.setEnabled(false);

        brandRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewBrands);
        brandArrayList = new ArrayList<>();
        brandRecyclerAdapter = new BrandRecyclerAdapter(this, brandArrayList, PicassoClient.getPicasso(), new OnBrandClickListener() {
            @Override
            public void onBrandClick(Brand brand) {
                brandClicked(brand);
            }
        });
        brandRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        brandRecyclerView.addItemDecoration(new VerticalSpacingItemDecoration(Utility.dpToPx(this, 20)));
        brandRecyclerView.setAdapter(brandRecyclerAdapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading Brands");
    }

    private void setupDropbox() {
        PicassoClient.init(getApplicationContext(), DropboxClientFactory.getClient(this));
    }

    private void loadBrandFolders() {
        if (!Utility.isInternetConnected(this, true)) {
            swipeRefreshLayout.setRefreshing(false);
            progressDialog.dismiss();
            if (brandArrayList.size() == 0) {
                textViewSwipeHint.setVisibility(View.VISIBLE);
                textViewDescription.setVisibility(View.INVISIBLE);
            } else {
                textViewSwipeHint.setVisibility(View.GONE);
                textViewDescription.setVisibility(View.VISIBLE);
            }
            return;
        }

        swipeRefreshLayout.setRefreshing(true);
        textViewSwipeHint.setVisibility(View.GONE);
        if (brandArrayList.size() == 0) {
            progressDialog.show();
        }

        if (listFolderTask != null) {
            listFolderTask.cancel(true);
        }

        listFolderTask = new ListFolderTask(DropboxClientFactory.getClient(this), new ListFolderTask.Callback() {
            @Override
            public void onDataLoaded(ListFolderResult result) {
                swipeRefreshLayout.setRefreshing(false);
                progressDialog.dismiss();
                brandArrayList.clear();
                if (result != null) {
                    List<Metadata> entities = result.getEntries();
                    if (entities != null) {
                        ArrayList<FileMetadata> logoEntities = new ArrayList<>();
                        for (int i = 0; i < entities.size(); i++) {
                            Metadata entity = entities.get(i);
                            if (entity instanceof FolderMetadata) {
                                brandArrayList.add(new Brand(entity.getName(), entity.getPathLower()));
                            } else if (entity instanceof FileMetadata) {
                                if (isImage(entity)) {
                                    logoEntities.add((FileMetadata) entity);
                                }
                            }
                        }
                        for (int i = 0; i < logoEntities.size(); i++) {
                            String imageName = logoEntities.get(i).getName();
                            imageName = imageName.substring(0, imageName.lastIndexOf("."));
                            for (int j = 0; j < brandArrayList.size(); j++) {
                                if (brandArrayList.get(j).getName().equalsIgnoreCase(imageName)) {
                                    brandArrayList.get(j).setLogoPath(logoEntities.get(i).getPathLower());
                                    break;
                                }
                            }
                        }
                        textViewDescription.setVisibility(View.VISIBLE);
                        Collections.sort(brandArrayList, new Brand.Comparator());
                        brandRecyclerAdapter.notifyDataSetChanged();
                        updateVideosCount(true);
                        return;
                    }
                }

                Utility.showToast(MainActivity.this, getString(R.string.unable_to_get_brands));
                brandRecyclerAdapter.notifyDataSetChanged();
                textViewSwipeHint.setVisibility(View.VISIBLE);
                textViewDescription.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onError(Exception e) {
                swipeRefreshLayout.setRefreshing(false);
                progressDialog.dismiss();
                textViewSwipeHint.setVisibility(View.VISIBLE);
                textViewDescription.setVisibility(View.INVISIBLE);

                Log.e(TAG, "Failed to list folder.", e);
                Utility.showToast(MainActivity.this, getString(R.string.unable_to_get_brands));
            }
        });
        listFolderTask.execute("");
    }

    private void loadBrandFoldersOffline() {
        swipeRefreshLayout.setRefreshing(false);
        textViewSwipeHint.setVisibility(View.GONE);
        brandArrayList.clear();

        ArrayList<BrandTitle> brandTitles = OfflineDataLoader.brandTitlesList();
        for (BrandTitle brandTitle: brandTitles) {
            brandArrayList.add(new Brand(brandTitle.getTitle(), brandTitle.getLogoRes()));
        }

        textViewDescription.setVisibility(View.VISIBLE);
        Collections.sort(brandArrayList, new Brand.Comparator());
        brandRecyclerAdapter.notifyDataSetChanged();
        updateVideosCount(true);
    }

    private boolean isImage(Metadata metadata) {
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String fileExtension = metadata.getName().substring(metadata.getName().lastIndexOf(".") + 1);
        String mimeType = mimeTypeMap.getMimeTypeFromExtension(fileExtension);
        return mimeType != null && mimeType.startsWith("image/");
    }

    private void updateVideosCount(boolean initiateDownload) {
        if (brandRecyclerAdapter != null && brandArrayList != null && brandArrayList.size() > 0) {
            int totalVideos = 0;
            int totalDownloaded = 0;
            brandVideosToDownload = new ArrayList<>();
            for (int i = 0; i < brandArrayList.size(); i++) {
                Brand brand = brandArrayList.get(i);
                String brandPath = "/" + brand.getName().toLowerCase() + "/";
                ArrayList<VideoTitle> videosTitles = OfflineDataLoader.videoTitlesList(brand.getName());
                int downloaded = 0;
                ArrayList<VideoToDownload> videosToDownload = new ArrayList<>();
                for (VideoTitle videoTitle: videosTitles) {
                    String title = videoTitle.getTitle()+".mp4";
                    Video.Status status = FileLoader.getVideoStatus(this, brand.getName(), title);
                    if (status == Video.Status.Downloaded || status == Video.Status.Outdated) {
                        downloaded++;
                    } else {
                        String path = brandPath+title.toLowerCase();
                        videosToDownload.add(new VideoToDownload(title, path));
                    }
                }
                totalVideos += videosTitles.size();
                totalDownloaded += downloaded;
                brand.setVideos(videosTitles.size(), downloaded);
                if (videosToDownload.size() > 0) {
                    brandVideosToDownload.add(new BrandVideosToDownload(brand.getName(), videosToDownload));
                }
            }
            brandRecyclerAdapter.notifyDataSetChanged();
            if (initiateDownload && brandVideosToDownload.size() > 0) {
                showVideosToDownloadDialog(totalDownloaded, totalVideos - totalDownloaded);
            }
        }
    }

    private void showVideosToDownloadDialog(int downloaded, int toBeDownloaded) {
        String videosAvailableForDownload = "";
        if (toBeDownloaded > 1) {
            String videosCount = toBeDownloaded + (downloaded > 0 ? " new" : "");
            videosAvailableForDownload = getString(R.string.text__available_for_download_plural, videosCount);
        } else {
            videosAvailableForDownload = getString(R.string.text__available_for_download_singular);
        }
        new AlertDialog.Builder(this).setMessage(videosAvailableForDownload).setPositiveButton(R.string.download_now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startDownloadingVideosInBackground();
            }
        }).setNegativeButton(R.string.download_later, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void startDownloadingVideosInBackground() {
        if (!Utility.isInternetConnected(this, false)) {
            Utility.showSnackbar(brandRecyclerView, R.string.connect_to_internet, R.string.try_again, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startDownloadingVideosInBackground();
                }
            });
            return;
        }

        if (downloadMultipleVideosTask != null) {
            downloadMultipleVideosTask.cancel(true);
        }

        downloadMultipleVideosTask = new DownloadMultipleVideosTask(this, DropboxClientFactory.getClient(this), brandVideosToDownload, new DownloadMultipleVideosTask.Callback() {
            @Override
            public void onDownloadComplete(int downloaded, int total) {
                sendDownloadCompleteBroadcast(downloaded, total);
            }
        });
        downloadMultipleVideosTask.execute();
    }

    private void sendDownloadCompleteBroadcast(int downloaded, int total) {
        Intent intent = new Intent(Consts.Actions.VIDEOS_DOWNLOADED);
        intent.putExtra(Consts.Extras.DOWNLOADED, downloaded);
        intent.putExtra(Consts.Extras.TOTAL, total);
        sendOrderedBroadcast(intent, null);
    }

    private void showDownloadCompleteDialog(int downloaded, int total) {
        if (downloaded > 0 && total > 0 && total >= downloaded) {
            new AlertDialog.Builder(this).setMessage(getString(R.string.successfully_downloaded_videos, downloaded, total)).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        }
    }

    private void brandClicked(Brand brand) {
        Intent intent = new Intent(this, VideoListActivity.class);
        intent.putExtra(Consts.Extras.BRAND, brand);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateVideosCount(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(Consts.Actions.VIDEOS_DOWNLOADED);
        intentFilter.setPriority(1);
        registerReceiver(videosDownloadedReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(videosDownloadedReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listFolderTask != null) {
            listFolderTask.cancel(true);
        }
    }

    private BroadcastReceiver videosDownloadedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                int downloaded = extras.getInt(Consts.Extras.DOWNLOADED, 0);
                int total = extras.getInt(Consts.Extras.TOTAL, 0);
                showDownloadCompleteDialog(downloaded, total);
            }
            abortBroadcast();
        }
    };

}
