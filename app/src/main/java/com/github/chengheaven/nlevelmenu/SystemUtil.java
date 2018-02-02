package com.github.chengheaven.nlevelmenu;

import android.content.Context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * @author Heaven_Cheng Created on 2018/2/2.
 */
public class SystemUtil {

    public static String getAssetsFile(Context context, String fileName) {
        InputStream inputStream;
        String s;
        try {
            inputStream = context.getAssets().open(fileName);
            int size = inputStream.available();
            byte[] bytes = new byte[size];
            inputStream.read(bytes);
            inputStream.close();
            s = new String(bytes);
            return s;
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public static <T> List<T> deepCopy(List<T> src) {

        List<T> dest = null;
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out;
            out = new ObjectOutputStream(byteOut);
            out.writeObject(src);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            //noinspection unchecked
            dest = (List<T>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return dest;
    }
}
