package com.e2esp.nestlemythbusting.helpers;

import com.e2esp.nestlemythbusting.R;
import com.e2esp.nestlemythbusting.models.Brand;
import com.e2esp.nestlemythbusting.models.BrandTitle;
import com.e2esp.nestlemythbusting.models.VideoTitle;

import java.util.ArrayList;

/**
 * Created by Zain on 4/24/2017.
 */

public class OfflineDataLoader {

    public static ArrayList<BrandTitle> brandTitlesList() {
        ArrayList<BrandTitle> brandTitlesList = new ArrayList<>();
        brandTitlesList.add(new BrandTitle("NESTLÉ BUNYAD", R.drawable.nestle_bunyad));
        brandTitlesList.add(new BrandTitle("NESTLÉ EVERYDAY", R.drawable.nestle_everyday));
        brandTitlesList.add(new BrandTitle("NESTLÉ FRUITA VITALS", R.drawable.nestle_juices));
        brandTitlesList.add(new BrandTitle("NESTLÉ MAGGI", R.drawable.nestle_maggi));
        brandTitlesList.add(new BrandTitle("NESTLÉ MILKPAK", R.drawable.nestle_milkpak));
        brandTitlesList.add(new BrandTitle("NESTLÉ MILKPAK YOGURT", R.drawable.nestle_milkpak_yogurt));
        brandTitlesList.add(new BrandTitle("NESTLÉ NESCAFÉ", R.drawable.nestle_nescafe));
        brandTitlesList.add(new BrandTitle("NESTLÉ NIDO FORTIGROW", R.drawable.nestle_nido_fortigrow));
        brandTitlesList.add(new BrandTitle("NESTLÉ NIDO GUMS", R.drawable.nestle_nido_gums));
        brandTitlesList.add(new BrandTitle("NESTLÉ PURE LIFE", R.drawable.nestle_pure_life));
        brandTitlesList.add(new BrandTitle("NESTLÉ Corporate", R.drawable.nestle_corporate));
        return brandTitlesList;
    }

    public static ArrayList<VideoTitle> videoTitlesList(String brand) {
        ArrayList<VideoTitle> videoTitlesList = new ArrayList<>();
        switch (brand) {
            case "NESTLÉ BUNYAD":
                //videoTitlesList.add(new VideoTitle("Is it Milk", "Is NESTLÉ BUNYAD milk?"));
                break;
            case "NESTLÉ EVERYDAY":
                //videoTitlesList.add(new VideoTitle("Safe for Consumption", "Is EVERYDAY Milk? If not, than is it safe for drinking/direct consumption?"));
                break;
            case "NESTLÉ FRUITA VITALS":
                //videoTitlesList.add(new VideoTitle("Juices vs Nectars", "Difference between Juices & Nectars"));
                videoTitlesList.add(new VideoTitle("Quality assurance", "How do we ensure the quality of NESTLÉ FRUITA VITALS?"));
                videoTitlesList.add(new VideoTitle("Project TRUST Documentary", "Journey of Quality"));
                break;
            case "NESTLÉ MAGGI":
                videoTitlesList.add(new VideoTitle("MAGGI and health", "Is MAGGI healthy?"));
                videoTitlesList.add(new VideoTitle("Crisp and Shiny", "Why is the noodle cake crispy and shiny?"));
                videoTitlesList.add(new VideoTitle("MAGGI tastemaker", "What is inside the taste maker?"));
                break;
            case "NESTLÉ MILKPAK":
                //videoTitlesList.add(new VideoTitle("Balai Cream", "Why there is no Balai/Cream on NESTLÉ MILKPAK. Why cant one make homemade yogurt from NESTLÉ MILKPAK?"));
                //videoTitlesList.add(new VideoTitle("Long Shelf Life", "How does NESTLÉ MILKPAK have a such a long shelf life? Does it have any preservatives or chemicals?"));
                break;
            case "NESTLÉ MILKPAK YOGURT":
                videoTitlesList.add(new VideoTitle("UHT Milk or Pasteurized", "Is Yogurt made from UHT milk or pasteurized milk?"));
                videoTitlesList.add(new VideoTitle("YOGURT Long Shelf Life", "How does yogurt have a long shelf life? Does Yogurt have any preservatives?"));
                break;
            case "NESTLÉ NESCAFÉ":
                videoTitlesList.add(new VideoTitle("Skin related issue", "Is it possible to have skin related or complexion issues after having coffee?"));
                videoTitlesList.add(new VideoTitle("Garam Taseer", "Does coffee have Garam Taseer (heaty perception)?"));
                break;
            case "NESTLÉ NIDO FORTIGROW":
                //videoTitlesList.add(new VideoTitle("How is it made", "How is NIDO (powder milk) made. How do we fortify it?"));
                //videoTitlesList.add(new VideoTitle("OK for Kids", "Is it ok to give kids above 3yrs powder milk? Clarify the misconceptions on giving powder milk to children 3+ years."));
                break;
            case "NESTLÉ NIDO GUMS":
                videoTitlesList.add(new VideoTitle("Pre Pro Biotics", "What are Pre/Pro Biotics and how do they effect the baby's system?"));
                break;
            case "NESTLÉ PURE LIFE":
                videoTitlesList.add(new VideoTitle("Plain water vs Mineral water", "Is there a difference between plain water and mineral water?"));
                break;
            case "NESTLÉ Corporate":
                //videoTitlesList.add(new VideoTitle("Emulsifiers and Stabilizers", "What are Emulsifiers & Stabilizers and why are they used in our brands?"));
                //videoTitlesList.add(new VideoTitle("Milk Powders", "What different types of milk powders exist?"));
                videoTitlesList.add(new VideoTitle("Nestle Products are Halal", "Are Nestlé Pakistan products Halal? How do you ensure they are Halal?"));
                //videoTitlesList.add(new VideoTitle("Vegetable Fat", "Clarify misconceptions around use of vegetable fat. Is it bad for health? Why is it used?"));
                break;
        }
        return videoTitlesList;
    }

}
