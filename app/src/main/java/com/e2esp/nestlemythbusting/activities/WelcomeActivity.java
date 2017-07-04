package com.e2esp.nestlemythbusting.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.e2esp.nestlemythbusting.R;
import com.e2esp.nestlemythbusting.utils.Consts;
import com.e2esp.nestlemythbusting.utils.Utility;
import io.fabric.sdk.android.Fabric;

/**
 * Created by Zain on 3/22/2017.
 */

public class WelcomeActivity extends AppCompatActivity {

    private Typeface font;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_welcome);

        font = Utility.getArialFont(this);

        TextView textViewWelcome = (TextView) findViewById(R.id.textViewWelcome);
        textViewWelcome.setTypeface(font);

        AppCompatButton buttonAccessApp = (AppCompatButton) findViewById(R.id.buttonAccessApp);
        buttonAccessApp.setTypeface(font);
        buttonAccessApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAccessAppButtonClick();
            }
        });
    }

    private void onAccessAppButtonClick() {
        if (!Utility.Prefs.getPrefBool(this, Consts.Keys.APP_ACCESS, false)) {
            showPasscodeDialog();
            return;
        }
        startActivity(new Intent(this, MainActivity.class));
    }

    private void showPasscodeDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_passcode);

        TextView textViewPasscodeRequired = (TextView) dialog.findViewById(R.id.textViewPasscodeRequired);
        textViewPasscodeRequired.setTypeface(font);

        TextView textViewPasscodeExplanation = (TextView) dialog.findViewById(R.id.textViewPasscodeExplanation);
        textViewPasscodeExplanation.setTypeface(font);

        final AppCompatEditText editTextPasscode = (AppCompatEditText) dialog.findViewById(R.id.editTextPasscode);
        editTextPasscode.setTypeface(font);

        AppCompatButton buttonPasscodeContinue = (AppCompatButton) dialog.findViewById(R.id.buttonPasscodeContinue);
        buttonPasscodeContinue.setTypeface(font);
        buttonPasscodeContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String passcode = editTextPasscode.getText().toString();
                if (passcode.equals(Consts.AppPassCode)) {
                    Utility.Prefs.setPref(WelcomeActivity.this, Consts.Keys.APP_ACCESS, true);
                    onAccessAppButtonClick();
                    dialog.dismiss();
                } else {
                    editTextPasscode.setError(getString(R.string.invalid_code));
                    editTextPasscode.requestFocus();
                    return;
                }
            }
        });

        dialog.show();
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
