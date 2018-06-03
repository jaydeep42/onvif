package com.example.android.onifcamerademojava.util;

import android.graphics.Bitmap;
import android.util.SparseArray;

import com.google.android.gms.vision.face.Face;


/**
 * Created by vardan on 6/2/18.
 * Util class for extracting faces from the image.
 */

public class FaceUtil {
    private FaceUtil() {
    }

    /**
     * Saves original image and detected faces in the given path.
     *
     * @param faces  array of detected faces
     * @param bitmap original image from which faces are extracted
     * @param path   where to save detected faces and original image
     */
    public static void save(SparseArray<Face> faces, Bitmap bitmap, String path) {
        FileUtil.saveBitmap(bitmap, path);
        for (int i = 0; i < faces.size(); ++i) {
            Face face = faces.valueAt(i);
            int x = (int) face.getPosition().x;
            int y = (int) face.getPosition().x;
            int width = (int) face.getWidth();
            int height = (int) face.getHeight();
            int move = 250; // y move factor depends on height of the status bar
            if (move + height > bitmap.getHeight()) {
                move = 0;
            }

            Bitmap mbitmap = Bitmap.createBitmap(bitmap, x, y + move, width, height);

            FileUtil.saveBitmap(mbitmap, path);
        }

    }
}
