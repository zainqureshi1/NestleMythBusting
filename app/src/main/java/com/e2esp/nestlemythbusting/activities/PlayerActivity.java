package com.e2esp.nestlemythbusting.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.e2esp.nestlemythbusting.R;
import com.e2esp.nestlemythbusting.applications.NestleApplication;
import com.e2esp.nestlemythbusting.models.Brand;
import com.e2esp.nestlemythbusting.models.Video;
import com.e2esp.nestlemythbusting.utils.Consts;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;

public class PlayerActivity extends AppCompatActivity {

    private VideoView videoViewPlayer;

    private Brand brand;
    private Video video;

    private boolean isFullscreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        brand = intent.getParcelableExtra(Consts.Extras.BRAND);
        video = intent.getParcelableExtra(Consts.Extras.VIDEO);
        if (brand == null || video == null) {
            return;
        }

        setContentView(R.layout.activity_player);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(video.getTitleWithoutExt());

        setupView();
        setupVideo();
        sendAnalyticsScreenHit();
    }

    private void setupView() {
        videoViewPlayer = (VideoView) findViewById(R.id.videoViewPlayer);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoViewPlayer);
        videoViewPlayer.setMediaController(mediaController);

        videoViewPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoCompleted();
            }
        });
    }

    private void setupVideo() {
        File brandFolder = getBrandFolder();
        if (brandFolder == null) {
            return;
        }
        File videoFile = new File(brandFolder, video.getTitle());
        if (!videoFile.exists()) {
            return;
        }

        videoViewPlayer.setVideoPath(videoFile.getAbsolutePath());
        videoViewPlayer.start();
    }

    public void onFullScreenClicked(View view) {
        toggleFullscreen();
    }

    private void toggleFullscreen() {
        if (isFullscreen) {
            isFullscreen = false;
            getSupportActionBar().show();
            ViewGroup.LayoutParams videoParams = videoViewPlayer.getLayoutParams();
            videoParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            videoParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            videoViewPlayer.setLayoutParams(videoParams);
        } else {
            isFullscreen = true;
            getSupportActionBar().hide();
            ViewGroup.LayoutParams videoParams = videoViewPlayer.getLayoutParams();
            videoParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            videoParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            videoViewPlayer.setLayoutParams(videoParams);
        }
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

    private void videoCompleted() {
        finish();
    }

    private void sendAnalyticsScreenHit() {
        Tracker tracker = ((NestleApplication)getApplication()).getTracker();
        tracker.setScreenName(brand.getName()+ " :: " + video.getTitle() + " Player Screen");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
        tracker.setScreenName(null);
    }

    private boolean backPressed = false;
    @Override
    public void onBackPressed() {
        if (isFullscreen) {
            toggleFullscreen();
            return;
        }

        if (backPressed) {
            super.onBackPressed();
            return;
        }

        backPressed = true;
        Toast.makeText(this, getString(R.string.press_back_again_to_close), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                backPressed = false;
            }
        }, 2000);
    }

}
