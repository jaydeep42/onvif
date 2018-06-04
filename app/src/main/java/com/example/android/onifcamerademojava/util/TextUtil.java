package com.example.android.onifcamerademojava.util;

import android.util.SparseArray;
import com.google.android.gms.vision.text.TextBlock;

/**
 * Created by vardan on 6/4/18.
 * Util class for concatenating texts array into single string.
 */

public class TextUtil {
    private TextUtil(){}

    /**
     *
     * @param texts array of detected texts
     * @return concatenated detected texts string
     */
    public static String concatenate(SparseArray<TextBlock> texts) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < texts.size(); ++i) {
            stringBuilder.append(texts.get(i).getValue());
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }
}
