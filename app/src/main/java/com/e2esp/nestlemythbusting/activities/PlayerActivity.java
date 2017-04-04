package com.e2esp.nestlemythbusting.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.e2esp.nestlemythbusting.R;
import com.e2esp.nestlemythbusting.applications.NestleApplication;
import com.e2esp.nestlemythbusting.models.Brand;
import com.e2esp.nestlemythbusting.models.Video;
import com.e2esp.nestlemythbusting.utils.Consts;
import com.e2esp.nestlemythbusting.views.CustomVideoView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;

public class PlayerActivity extends AppCompatActivity {

    private CustomVideoView videoViewPlayer;
    private Button buttonFullScreen;

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_player);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(video.getTitleWithoutExt());

        setupView();
        setupVideo();
        toggleFullscreen();
        sendAnalyticsScreenHit();
    }

    private void setupView() {
        videoViewPlayer = (CustomVideoView) findViewById(R.id.videoViewPlayer);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoViewPlayer);
        videoViewPlayer.setMediaController(mediaController);

        videoViewPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoCompleted();
            }
        });

        buttonFullScreen = (Button) findViewById(R.id.buttonFullScreen);
        buttonFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFullscreen();
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

    private void toggleFullscreen() {
        if (isFullscreen) {
            isFullscreen = false;
            getSupportActionBar().show();
            //exitFullscreen();
            ViewGroup.LayoutParams videoParams = videoViewPlayer.getLayoutParams();
            videoParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            videoParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            videoViewPlayer.setLayoutParams(videoParams);
            buttonFullScreen.setBackgroundResource(R.drawable.fullscreen_open);
        } else {
            isFullscreen = true;
            getSupportActionBar().hide();
            //enterFullscreen();
            ViewGroup.LayoutParams videoParams = videoViewPlayer.getLayoutParams();
            videoParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            videoParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            videoViewPlayer.setLayoutParams(videoParams);
            buttonFullScreen.setBackgroundResource(R.drawable.fullscreen_close);
        }
    }

    private void enterFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View mDecorView = getWindow().getDecorView();
            mDecorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }

    private void exitFullscreen() {
        View mDecorView = getWindow().getDecorView();
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
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
