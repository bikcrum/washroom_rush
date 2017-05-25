package com.android.washroomrush;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HelperClass {

    static String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    static Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }



    static <E> E mostFrequentValue(List<E> values) {
        HashMap<E, Integer> map = new HashMap<>();
        for (int i = 0; i < values.size(); i++) {
            E val = values.get(i);
            if (map.containsKey(val)) {
                map.put(val, map.get(val) + 1);
            } else {
                map.put(val, 1);
            }
        }
        Map.Entry<E, Integer> maxEntry = null;

        for (Map.Entry<E, Integer> entry : map.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        }
        return maxEntry.getKey();
    }




    static Bitmap getMarkerIconAccordingToRating(Resources resources, float rating) {
        BitmapDrawable bitmapDrawable;
        if (rating >= 4.5) {
            bitmapDrawable = (BitmapDrawable) resources.getDrawable(R.drawable.toilet_marker6);
        } else if (rating >= 4.0) {
            bitmapDrawable = (BitmapDrawable) resources.getDrawable(R.drawable.toilet_marker5);
        } else if (rating >= 3.5) {
            bitmapDrawable = (BitmapDrawable) resources.getDrawable(R.drawable.toilet_marker4);
        } else if (rating >= 3.0) {
            bitmapDrawable = (BitmapDrawable) resources.getDrawable(R.drawable.toilet_marker3);
        } else if (rating >= 2.5) {
            bitmapDrawable = (BitmapDrawable) resources.getDrawable(R.drawable.toilet_marker2);
        } else if (rating >= 1.5) {
            bitmapDrawable = (BitmapDrawable) resources.getDrawable(R.drawable.toilet_marker1);
        } else {
            bitmapDrawable = (BitmapDrawable) resources.getDrawable(R.drawable.toilet_marker0);
        }
        return bitmapDrawable.getBitmap();
    }

    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, 0, 0, null);
        return bmOverlay;
    }
}

