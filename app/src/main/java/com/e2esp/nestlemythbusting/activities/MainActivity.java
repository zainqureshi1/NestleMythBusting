package com.e2esp.nestlemythbusting.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.e2esp.nestlemythbusting.models.Brand;
import com.e2esp.nestlemythbusting.utils.Consts;
import com.e2esp.nestlemythbusting.utils.DropboxClientFactory;
import com.e2esp.nestlemythbusting.utils.ListFolderTask;
import com.e2esp.nestlemythbusting.utils.PicassoClient;
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
        DropboxClientFactory.init(getString(R.string.dropbox_access_token));
        PicassoClient.init(getApplicationContext(), DropboxClientFactory.getClient());
    }

    private void loadBrandFolders() {
        if (!Utility.isInternetConnected(this, true)) {
            swipeRefreshLayout.setRefreshing(false);
            progressDialog.dismiss();
            textViewSwipeHint.setVisibility(View.VISIBLE);
            textViewDescription.setVisibility(View.INVISIBLE);
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

        listFolderTask = new ListFolderTask(DropboxClientFactory.getClient(), new ListFolderTask.Callback() {
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
                        updateVideosCount();
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

        brandArrayList.add(new Brand("Nestlé BUNYAD", R.drawable.nestle_bunyad));
        brandArrayList.add(new Brand("Nestlé EVERYDAY", R.drawable.nestle_everyday));
        brandArrayList.add(new Brand("Nestlé JUICES", R.drawable.nestle_juices));
        brandArrayList.add(new Brand("Nestlé MAGGI", R.drawable.nestle_maggi));
        brandArrayList.add(new Brand("Nestlé MILKPAK", R.drawable.nestle_milkpak));
        brandArrayList.add(new Brand("Nestlé MILKPAK YOGURT", R.drawable.nestle_milkpak_yogurt));
        brandArrayList.add(new Brand("Nestlé NESCAFÉ", R.drawable.nestle_nescafe));
        brandArrayList.add(new Brand("Nestlé NIDO FORTIGROW", R.drawable.nestle_nido_fortigrow));
        brandArrayList.add(new Brand("Nestlé NIDO GUMS", R.drawable.nestle_nido_gums));
        brandArrayList.add(new Brand("Nestlé PURE LIFE", R.drawable.nestle_pure_life));

        textViewDescription.setVisibility(View.VISIBLE);
        Collections.sort(brandArrayList, new Brand.Comparator());
        brandRecyclerAdapter.notifyDataSetChanged();
        updateVideosCount();
    }

    private boolean isImage(Metadata metadata) {
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String fileExtension = metadata.getName().substring(metadata.getName().lastIndexOf(".") + 1);
        String mimeType = mimeTypeMap.getMimeTypeFromExtension(fileExtension);
        return mimeType != null && mimeType.startsWith("image/");
    }

    private void updateVideosCount() {
        if (brandRecyclerAdapter != null && brandArrayList != null && brandArrayList.size() > 0) {
            for (int i = 0; i < brandArrayList.size(); i++) {
                Brand brand = brandArrayList.get(i);
                int total = Utility.Prefs.getPrefInt(MainActivity.this, brand.getName()+Consts.Keys.TOTAL_VIDEOS, 0);
                int downloaded = Utility.Prefs.getPrefInt(MainActivity.this, brand.getName()+Consts.Keys.DOWNLOADED_VIDEOS, 0);
                brand.setVideos(total, downloaded);
            }
            brandRecyclerAdapter.notifyDataSetChanged();
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
        updateVideosCount();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listFolderTask != null) {
            listFolderTask.cancel(true);
        }
    }

}
