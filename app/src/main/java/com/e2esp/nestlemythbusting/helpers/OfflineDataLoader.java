package com.e2esp.nestlemythbusting.helpers;

import com.e2esp.nestlemythbusting.models.Title;

import java.util.ArrayList;

/**
 * Created by Zain on 4/24/2017.
 */

public class OfflineDataLoader {

    public static ArrayList<Title> videoTitlesList(String brand) {
        ArrayList<Title> videoTitlesList = new ArrayList<>();
        switch (brand) {
            case "NESTLÉ BUNYAD":
                //videoTitlesList.add(new Title("Is it Milk", "Is NESTLÉ BUNYAD milk?"));
                break;
            case "NESTLÉ Corporate":
                //videoTitlesList.add(new Title("Emulsifiers and Stabilizers", "What are Emulsifiers & Stabilizers and why are they used in our brands?"));
                //videoTitlesList.add(new Title("Milk Powders", "What different types of milk powders exist?"));
                videoTitlesList.add(new Title("Nestle Products are Halal", "Are Nestlé Pakistan products Halal? How do you ensure they are Halal?"));
                //videoTitlesList.add(new Title("Vegetable Fat", "Clarify misconceptions around use of vegetable fat. Is it bad for health? Why is it used?"));
                break;
            case "NESTLÉ EVERYDAY":
                //videoTitlesList.add(new Title("Safe for Consumption", "Is EVERYDAY Milk? If not, than is it safe for drinking/direct consumption?"));
                break;
            case "NESTLÉ FRUITA VITALS":
                //videoTitlesList.add(new Title("Juices vs Nectars", "Difference between Juices & Nectars"));
                videoTitlesList.add(new Title("Quality assurance", "How do we ensure the quality of NESTLÉ FRUITA VITALS?"));
                videoTitlesList.add(new Title("Project TRUST Documentary", "Journey of Quality"));
                break;
            case "NESTLÉ MAGGI":
                videoTitlesList.add(new Title("MAGGI and health", "Is MAGGI healthy?"));
                videoTitlesList.add(new Title("Crisp and Shiny", "Why is the noodle cake crispy and shiny?"));
                videoTitlesList.add(new Title("MAGGI tastemaker", "What is inside the taste maker?"));
                break;
            case "NESTLÉ MILKPAK":
                //videoTitlesList.add(new Title("Balai Cream", "Why there is no Balai/Cream on NESTLÉ MILKPAK. Why cant one make homemade yogurt from NESTLÉ MILKPAK?"));
                //videoTitlesList.add(new Title("Long Shelf Life", "How does NESTLÉ MILKPAK have a such a long shelf life? Does it have any preservatives or chemicals?"));
                break;
            case "NESTLÉ MILKPAK YOGURT":
                videoTitlesList.add(new Title("UHT Milk or Pasteurized", "Is Yogurt made from UHT milk or pasteurized milk?"));
                videoTitlesList.add(new Title("YOGURT Long Shelf Life", "How does yogurt have a long shelf life? Does Yogurt have any preservatives?"));
                break;
            case "NESTLÉ NESCAFÉ":
                videoTitlesList.add(new Title("Skin related issue", "Is it possible to have skin related or complexion issues after having coffee?"));
                videoTitlesList.add(new Title("Garam Taseer", "Does coffee have Garam Taseer (heaty perception)?"));
                break;
            case "NESTLÉ NIDO FORTIGROW":
                //videoTitlesList.add(new Title("How is it made", "How is NIDO (powder milk) made. How do we fortify it?"));
                //videoTitlesList.add(new Title("OK for Kids", "Is it ok to give kids above 3yrs powder milk? Clarify the misconceptions on giving powder milk to children 3+ years."));
                break;
            case "NESTLÉ NIDO GUMS":
                videoTitlesList.add(new Title("Pre Pro Biotics", "What are Pre/Pro Biotics and how do they effect the baby's system?"));
                break;
            case "NESTLÉ PURE LIFE":
                videoTitlesList.add(new Title("Plain water vs Mineral water", "Is there a difference between plain water and mineral water?"));
                break;
        }
        return videoTitlesList;
    }

    public static int getTotalVideosCount(String brand) {
        return videoTitlesList(brand).size();
    }

}
