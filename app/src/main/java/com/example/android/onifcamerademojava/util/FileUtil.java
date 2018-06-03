package com.example.android.onifcamerademojava.util;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Created by vardan on 6/1/18.
 * Util class for saving image to the given directory.
 */

public class FileUtil {

    private static final String TAG = "FileUtil";

    private FileUtil() {
    }

    /**
     * Saves bitmap to the file.
     *
     * @param bmp         image to be saved
     * @param whereToSave directory path where to be saved
     * @param fileName    image file name. Default name is date of creation.
     * @param format      image file format. Default format is png.
     */

    public static void saveBitmap(Bitmap bmp, String whereToSave, String fileName, Bitmap.CompressFormat format) {
        createDirectoryIfDoesNotExist(whereToSave);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(whereToSave + fileName + "." + format.toString());
            bmp.compress(format, 100, out);
            out.flush();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public static void saveBitmap(Bitmap bmp, String whereToSave) {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
        saveBitmap(bmp, whereToSave, now.toString(), Bitmap.CompressFormat.PNG);
    }

    public static void saveBitmap(Bitmap bmp, String whereToSave, String fileName) {
        saveBitmap(bmp, whereToSave, fileName, Bitmap.CompressFormat.PNG);
    }

    /**
     * Creates directory
     *
     * @param directory path to be created
     */
    private static void createDirectoryIfDoesNotExist(String directory) {
        File folder = new File(directory);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }
}
