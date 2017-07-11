package com.e2esp.nestlemythbusting.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.e2esp.nestlemythbusting.R;
import com.e2esp.nestlemythbusting.callbacks.OnBrandClickListener;
import com.e2esp.nestlemythbusting.models.Brand;
import com.e2esp.nestlemythbusting.tasks.FileThumbnailRequestHandler;
import com.e2esp.nestlemythbusting.utils.Utility;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Zain on 3/22/2017.
 */

public class BrandRecyclerAdapter extends RecyclerView.Adapter<BrandRecyclerAdapter.BrandViewHolder> {

    private Context context;
    private ArrayList<Brand> brandsList;
    private final Picasso picasso;
    private OnBrandClickListener onBrandClickListener;

    private Typeface font;

    public BrandRecyclerAdapter(Context context, ArrayList<Brand> brandsList, Picasso picasso, OnBrandClickListener onBrandClickListener) {
        this.context = context;
        this.brandsList = brandsList;
        this.picasso = picasso;
        this.onBrandClickListener = onBrandClickListener;
        this.font = Utility.getArialFont(context);
    }

    @Override
    public BrandViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.card_brand_layout, parent, false);
        return new BrandViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return brandsList.size();
    }

    @Override
    public void onBindViewHolder(BrandViewHolder holder, int position) {
        holder.bindView(brandsList.get(position));
    }

    public class BrandViewHolder extends RecyclerView.ViewHolder {

        private View topView;
        private ProgressBar progressBarLogo;
        private ImageView imageViewLogo;
        private TextView textViewName;
        private TextView textViewVideosCount;

        public BrandViewHolder(View itemView) {
            super(itemView);
            topView = itemView;
            progressBarLogo = (ProgressBar) itemView.findViewById(R.id.progressBarLogo);
            imageViewLogo = (ImageView) itemView.findViewById(R.id.imageViewBrandLogo);
            textViewName = (TextView) itemView.findViewById(R.id.textViewBrandName);
            textViewName.setTypeface(font);
            textViewName.setSelected(true);
            textViewVideosCount = (TextView) itemView.findViewById(R.id.textViewBrandVideosCount);
            textViewVideosCount.setTypeface(font);
        }

        public void bindView(final Brand brand) {
            textViewName.setText(brand.getName());
            if (brand.getTotalVideos() > 0) {
                textViewVideosCount.setText(brand.getDownloadedVideos() + " / " + brand.getTotalVideos() + " " + context.getString(R.string.videos_downloaded));
            } else {
                textViewVideosCount.setText("");
            }

            if (brand.getLogoRes() > 0) {
                progressBarLogo.setVisibility(View.GONE);
                imageViewLogo.setImageResource(brand.getLogoRes());
            } else {
                picasso.load(FileThumbnailRequestHandler.buildPicassoUri(brand.getLogoPath()))
                        .into(imageViewLogo, new Callback() {
                            @Override
                            public void onSuccess() {
                                progressBarLogo.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {
                                progressBarLogo.setVisibility(View.GONE);
                            }
                        });
            }

            topView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBrandClickListener.onBrandClick(brand);
                }
            });
        }

    }

}
