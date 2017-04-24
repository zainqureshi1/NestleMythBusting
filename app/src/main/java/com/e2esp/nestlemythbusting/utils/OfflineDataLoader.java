package com.e2esp.nestlemythbusting.utils;

import com.e2esp.nestlemythbusting.models.Title;

import java.util.ArrayList;

/**
 * Created by Zain on 4/24/2017.
 */

public class OfflineDataLoader {

    public static ArrayList<Title> videoTitlesList(String brand) {
        ArrayList<Title> videoTitlesList = new ArrayList<>();
        switch (brand) {
            case "Nestlé BUNYAD":
                videoTitlesList.add(new Title("Is it Milk", "Is NESTLÉ BUNYAD milk?"));
                break;
            case "Nestlé Corporate":
                videoTitlesList.add(new Title("Emulsifiers and Stabilizers", "What are Emulsifiers & Stabilizers and why are they used in our brands?"));
                videoTitlesList.add(new Title("Milk Powders", "What different types of milk powders exist?"));
                videoTitlesList.add(new Title("Nestle Products Halal", "Are Nestlé Pakistan products Halal? How do you ensure they are Halal?"));
                videoTitlesList.add(new Title("Vegetable Fat", "Clarify misconceptions around use of vegetable fat. Is it bad for health? Why is it used?"));
                break;
            case "Nestlé EVERYDAY":
                videoTitlesList.add(new Title("Safe for Consumption", "Is EVERYDAY Milk? If not, than is it safe for drinking/direct consumption?"));
                break;
            case "Nestlé JUICES":
                videoTitlesList.add(new Title("Juices vs Nectars", "Difference between Juices & Nectars"));
                break;
            case "Nestlé MAGGI":
                videoTitlesList.add(new Title("MSG and health", "Is MSG bad for health especially kids?"));
                break;
            case "Nestlé MILKPAK":
                videoTitlesList.add(new Title("Balai Cream", "Why there is no Balai/Cream on NESTLÉ MILKPAK. Why cant one make homemade yogurt from NESTLÉ MILKPAK?"));
                videoTitlesList.add(new Title("Long Shelf Life", "How does NESTLÉ MILKPAK have a such a long shelf life? Does it have any preservatives or chemicals?"));
                break;
            case "Nestlé MILKPAK YOGURT":
                videoTitlesList.add(new Title("How its made", "Is Yogurt made from UHT Milk or pasteurized milk"));
                videoTitlesList.add(new Title("Long Shelf Life", "How does yogurt have a long shelf life? Does Yogurt have any preservatives?"));
                break;
            case "Nestlé NESCAFÉ":
                videoTitlesList.add(new Title("Complexion issues", "Is it possible to have skin related or complexion issues after having coffee."));
                videoTitlesList.add(new Title("Garam Taseer", "Does coffee have Garam Taseer (heaty perception)"));
                break;
            case "Nestlé NIDO FORTIGROW":
                videoTitlesList.add(new Title("How is it made", "How is NIDO (powder milk) made. How do we fortify it?"));
                videoTitlesList.add(new Title("OK for Kids", "Is it ok to give kids above 3yrs powder milk? Clarify the misconceptions on giving powder milk to children 3+ years."));
                break;
            case "Nestlé NIDO GUMS":
                videoTitlesList.add(new Title("Pre Pro Biotics", "What are Pre/Pro Biotics and how do they effect the baby's system?"));
                break;
            case "Nestlé PURE LIFE":
                videoTitlesList.add(new Title("Plain Water vs Mineral Water", "Difference between plain water and Mineral water?"));
                break;
        }
        return videoTitlesList;
    }

    public static int getTotalVideosCount(String brand) {
        return videoTitlesList(brand).size();
    }

}
