package com.e2esp.nestlemythbusting.callbacks;

import com.e2esp.nestlemythbusting.models.Video;

/**
 * Created by Zain on 3/22/2017.
 */

public interface OnVideoClickListener {
    void onVideoClick(Video video);
    void onLongClick(Video video);
    void onDownloadClick(Video video);
}
