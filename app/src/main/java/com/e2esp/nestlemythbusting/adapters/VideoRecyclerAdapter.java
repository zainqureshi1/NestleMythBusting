package com.e2esp.nestlemythbusting.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.e2esp.nestlemythbusting.R;
import com.e2esp.nestlemythbusting.callbacks.OnVideoClickListener;
import com.e2esp.nestlemythbusting.models.Video;
import com.e2esp.nestlemythbusting.utils.Utility;

import java.util.ArrayList;

/**
 * Created by Zain on 3/22/2017.
 */

public class VideoRecyclerAdapter extends RecyclerView.Adapter<VideoRecyclerAdapter.VideoViewHolder> {

    private Context context;
    private ArrayList<Video> videosList;
    private OnVideoClickListener onVideoClickListener;

    private Typeface font;

    public VideoRecyclerAdapter(Context context, ArrayList<Video> videosList, OnVideoClickListener onVideoClickListener) {
        this.context = context;
        this.videosList = videosList;
        this.onVideoClickListener = onVideoClickListener;
        this.font = Utility.getArialFont(context);
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.card_video_layout, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return videosList.size();
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        holder.bindView(videosList.get(position));
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {

        private View topView;
        private ImageView imageViewPreview;
        private TextView textViewTitle;
        private TextView textViewStatus;
        private TextView textViewDownload;
        private ProgressBar progressBar;

        public VideoViewHolder(View itemView) {
            super(itemView);
            topView = itemView;
            imageViewPreview = (ImageView) itemView.findViewById(R.id.imageViewVideoPreview);
            textViewTitle = (TextView) itemView.findViewById(R.id.textViewVideoTitle);
            textViewTitle.setTypeface(font);
            textViewTitle.setSelected(true);
            textViewStatus = (TextView) itemView.findViewById(R.id.textViewVideoStatus);
            textViewStatus.setTypeface(font);
            textViewDownload = (TextView) itemView.findViewById(R.id.textViewVideoDownload);
            textViewDownload.setTypeface(font);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBarVideoProgress);
        }

        public void bindView(final Video video) {
            textViewTitle.setText(video.getTitleWithoutExt());
            switch (video.getStatus()) {
                case NotDownloaded:
                    textViewStatus.setText("");
                    textViewDownload.setVisibility(View.VISIBLE);
                    textViewDownload.setText(context.getString(R.string.download));
                    progressBar.setVisibility(View.GONE);
                    imageViewPreview.setImageResource(R.drawable.video_preview);
                    break;
                case Downloading:
                    textViewStatus.setText(video.getProgressText());
                    textViewDownload.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(video.getProgress());
                    imageViewPreview.setImageResource(R.drawable.video_preview);
                    break;
                case Downloaded:
                    textViewStatus.setText("");
                    textViewDownload.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.GONE);
                    Bitmap thumbnail = video.getThumbnail();
                    if (thumbnail != null) {
                        imageViewPreview.setImageBitmap(video.getThumbnail());
                    }
                    break;
                case Incomplete:
                    textViewStatus.setText(context.getString(R.string.incomplete));
                    textViewDownload.setVisibility(View.VISIBLE);
                    textViewDownload.setText(context.getString(R.string.download_again));
                    progressBar.setVisibility(View.GONE);
                    imageViewPreview.setImageResource(R.drawable.video_preview);
                    break;
                case Deleted:
                    textViewStatus.setText(context.getString(R.string.deleted));
                    textViewDownload.setVisibility(View.VISIBLE);
                    textViewDownload.setText(context.getString(R.string.download_again));
                    progressBar.setVisibility(View.GONE);
                    imageViewPreview.setImageResource(R.drawable.video_preview);
                    break;
            }

            topView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onVideoClickListener.onVideoClick(video);
                }
            });
            textViewDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onVideoClickListener.onDownloadClick(video);
                }
            });
        }

    }

}
