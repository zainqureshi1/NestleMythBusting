package com.e2esp.nestlemythbusting.applications;

import android.app.Application;

import com.e2esp.nestlemythbusting.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by Zain on 3/28/2017.
 */

public class NestleApplication extends Application {
    private Tracker mTracker;

    synchronized public Tracker getTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mTracker = analytics.newTracker(R.xml.screen_tracker);
        }
        return mTracker;
    }
}
