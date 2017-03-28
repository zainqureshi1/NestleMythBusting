package com.e2esp.nestlemythbusting.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.e2esp.nestlemythbusting.R;
import com.e2esp.nestlemythbusting.utils.Utility;

/**
 * Created by Zain on 3/22/2017.
 */

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_welcome);

        Typeface font = Utility.getArialFont(this);

        TextView textViewWelcome = (TextView) findViewById(R.id.textViewWelcome);
        textViewWelcome.setTypeface(font);

        AppCompatButton buttonLetsBegin = (AppCompatButton) findViewById(R.id.buttonLetsBegin);
        buttonLetsBegin.setTypeface(font);
        buttonLetsBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLetsBeginButtonClick();
            }
        });
    }

    private void onLetsBeginButtonClick() {
        startActivity(new Intent(this, MainActivity.class));
    }

    private boolean backPressed = false;
    @Override
    public void onBackPressed() {
        if (backPressed) {
            super.onBackPressed();
            return;
        }

        backPressed = true;
        Toast.makeText(this, getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                backPressed = false;
            }
        }, 2000);
    }

}
