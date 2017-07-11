package com.e2esp.nestlemythbusting.helpers;

import android.content.Context;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.e2esp.nestlemythbusting.R;

/**
 * Created by Zain on 3/24/2017.
 */

public class DropboxClientFactory {
    private static DbxClientV2 sDbxClient;

    private static void init(String accessToken) {
        if (sDbxClient == null) {
            DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("Nestle Android App")
                    .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                    .build();

            sDbxClient = new DbxClientV2(requestConfig, accessToken);
        }
    }

    public static DbxClientV2 getClient(Context context) {
        if (sDbxClient == null) {
            init(context.getString(R.string.dropbox_access_token));
        }
        return sDbxClient;
    }

}
