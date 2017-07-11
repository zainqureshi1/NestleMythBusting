package com.e2esp.nestlemythbusting.helpers;

import android.content.Context;
import android.util.Log;

import com.dropbox.core.v2.files.FileMetadata;
import com.e2esp.nestlemythbusting.models.Brand;
import com.e2esp.nestlemythbusting.tasks.DownloadFileTask;
import com.e2esp.nestlemythbusting.tasks.UploadFileTask;
import com.e2esp.nestlemythbusting.utils.Consts;
import com.e2esp.nestlemythbusting.utils.Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by Zain on 7/11/2017.
 */

public class FAQHandler {
    private final String TAG = "FAQHandler";

    private final String brandMarker = ">-BRAND: ";
    private final String questionMarker = "-QUESTION: ";

    private static FAQHandler instance;

    private Context context;
    private DownloadFileTask downloadFAQTask;
    private UploadFileTask uploadFAQTask;

    public static FAQHandler getInstance(Context context) {
        if (instance == null) {
            instance = new FAQHandler();
        }
        instance.context = context;
        return instance;
    }

    private boolean addQuestionInFAQFile(File file, Brand brand, String question) {
        String faq = readFAQFile(file);
        String dateTime = Utility.currentDateTimeString();
        String selectedBrandName = brand.getName();
        String selectedBrand = selectedBrandName;

        // split selected brand questions from all questions
        int brandIndex = -1;
        String[] brands = faq.split(brandMarker);
        for (int i = 0; i < brands.length; i++) {
            if (brands[i].startsWith(selectedBrandName)) {
                selectedBrand = brands[i];
                brandIndex = i;
                break;
            }
        }

        // remove previous last update time and add new time
        int index1 = selectedBrand.indexOf('(');
        int index2 = selectedBrand.indexOf(')');
        String newLastUpdate = "(last update: "+dateTime+")";
        if (index1 >= 0 && index2 > 0 & index1 < index2) {
            String toRemove = selectedBrand.substring(index1, index2+1);
            selectedBrand = selectedBrand.replace(toRemove, newLastUpdate);
        } else {
            String lastSegment = (selectedBrand.length() > selectedBrandName.length()+1) ? selectedBrand.substring(selectedBrandName.length()+1) : "";
            selectedBrand = selectedBrandName + " " + newLastUpdate + lastSegment;
        }

        // add new question and rejoin all questions
        selectedBrand = selectedBrand.trim() + "\n\n" + questionMarker + question;
        selectedBrand += " <" + dateTime + ">";
        if (brandIndex >= 0) {
            brands[brandIndex] = selectedBrand;
        }
        String newFAQ = "";
        for (int i = 0; i < brands.length; i++) {
            if (i > 0) {
                newFAQ += brandMarker;
            }
            newFAQ += brands[i];
        }
        if (brandIndex < 0) {
            if (!newFAQ.isEmpty()) {
                newFAQ = newFAQ.trim() + "\n\n\n";
            }
            newFAQ += brandMarker + selectedBrand;
        }

        // write questions to file
        return writeToFAQFile(newFAQ, file);
    }

    private boolean addSavedQuestionsInFAQFile(File file, String savedFaq) {
        String faq = readFAQFile(file);
        String dateTime = Utility.currentDateTimeString();

        String[] brands = faq.split(brandMarker);
        String[] savedBrands = savedFaq.split(brandMarker);
        ArrayList<String> newBrands = new ArrayList<>();

        for (int i = 1; i < savedBrands.length; i++) {
            String savedBrand = savedBrands[i];
            String savedBrandName = savedBrand;
            String savedBrandQuestions = savedBrand;
            int savedIndex1 = savedBrand.indexOf('(');
            int savedIndex2 = savedBrand.indexOf(')');
            if (savedIndex1 > 0) {
                savedBrandName = savedBrand.substring(0, savedIndex1-1);
            }
            if (savedIndex2 > 0) {
                savedBrandQuestions = savedBrand.substring(savedIndex2+1);
            }

            // get brand previous questions from faq
            String brand = savedBrand;
            int brandIndex = -1;
            for (int j = 0; j < brands.length; j++) {
                if (brands[j].startsWith(savedBrandName)) {
                    brand = brands[j];
                    brandIndex = j;
                    break;
                }
            }

            // remove previous last update time and add new time
            int index1 = brand.indexOf('(');
            int index2 = brand.indexOf(')');
            String newLastUpdate = "(last update: "+dateTime+")";
            if (index1 >= 0 && index2 > 0 & index1 < index2) {
                String toRemove = brand.substring(index1, index2+1);
                brand = brand.replace(toRemove, newLastUpdate);
            } else {
                String lastSegment = (brand.length() > savedBrandName.length()+1) ? brand.substring(savedBrandName.length()+1) : "";
                brand = savedBrandName + " " + newLastUpdate + lastSegment;
            }

            // add saved questions
            if (brandIndex >= 0) {
                brand = brand.trim() + "\n\n" + savedBrandQuestions.trim() + "\n\n\n";
                brands[brandIndex] = brand;
            } else {
                newBrands.add(brand);
            }
        }

        // rejoin all questions
        String newFAQ = "";
        for (int i = 0; i < brands.length; i++) {
            if (i > 0) {
                newFAQ += brandMarker;
            }
            newFAQ += brands[i];
        }

        // add any new brands
        for (int i = 0; i < newBrands.size(); i++) {
            if (!newFAQ.isEmpty()) {
                newFAQ = newFAQ.trim() + "\n\n\n";
            }
            newFAQ += brandMarker + newBrands.get(i);
        }

        // write questions to file
        return writeToFAQFile(newFAQ, file);
    }

    public void saveFAQ(Brand brand, String question) {
        File tempFile = FileLoader.getTEMPFile(context);
        addQuestionInFAQFile(tempFile, brand, question);

        showFAQSuccess();

        if (Utility.isInternetConnected(context, false)) {
            updateFAQ();
        }
    }

    public void updateFAQ() {
        if (!Utility.isInternetConnected(context, false)) {
            return;
        }
        File tempFile = FileLoader.getTEMPFile(context);
        final String savedFaq = readFAQFile(tempFile);
        Log.d(TAG, "Saved FAQ :: "+savedFaq);
        if (savedFaq.isEmpty()) {
            return;
        }

        if (downloadFAQTask != null) {
            downloadFAQTask.cancel(true);
        }

        File faqFile = FileLoader.getFAQFile(context);
        downloadFAQTask = new DownloadFileTask(context, DropboxClientFactory.getClient(context), faqFile, Consts.FAQFilePath, new DownloadFileTask.Callback() {
            @Override
            public void onDownloadComplete(File result) {
                if (addSavedQuestionsInFAQFile(result, savedFaq)) {
                    Log.d(TAG, "Added Saved FAQ :: Now Uploading");
                    uploadFAQ(result);
                } else {
                    Log.d(TAG, "Saved FAQ Not Added");
                }
            }
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
        downloadFAQTask.execute();
    }

    private String readFAQFile(File file) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append('\n');
            }
            bufferedReader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private boolean writeToFAQFile(String faq, File file) {
        FileOutputStream fileOutputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(faq);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (outputStreamWriter != null) {
                    outputStreamWriter.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void uploadFAQ(File file) {
        if (uploadFAQTask != null) {
            uploadFAQTask.cancel(true);
        }
        uploadFAQTask = new UploadFileTask(context, DropboxClientFactory.getClient(context), file, Consts.FAQFilePath, new UploadFileTask.Callback() {
            @Override
            public void onUploadComplete(FileMetadata result) {
                Log.d(TAG, "Uploaded New FAQ");
                File tempFile = FileLoader.getTEMPFile(context);
                tempFile.delete();
            }
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
        uploadFAQTask.execute();
    }

    private void showFAQSuccess() {
        Utility.showToast(context, "Successfully submitted question");
    }

}
