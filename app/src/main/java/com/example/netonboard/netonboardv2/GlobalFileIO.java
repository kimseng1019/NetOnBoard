package com.example.netonboard.netonboardv2;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Created by Netonboard on 26/1/2018.
 */

public class GlobalFileIO {
    Context context;
    static final String FILENAMEDOWN = "serverDown";
    static final String FILENAMEHISTORY = "serverHistory";
    static final String FILENAMESUPPORT = "support";
    static final String FILENAMENOTIFYTIME = "notifyTime";
    public static final String FILENAMEREMARK = "remark";
//    final File fileDown = new File(context.getFilesDir(), FILENAMEDOWN);
//    final File fileHistory = new File(context.getFilesDir(), FILENAMEHISTORY);
//    final File fileSupport = new File(context.getFilesDir(), FILENAMESUPPORT);
//    final File fileRemark = new File(context.getFilesDir(), FILENAMEREMARK);
//    final File fileNotifyTime = new File(context.getFilesDir(), FILENAMENOTIFYTIME);

    public GlobalFileIO(Context context) {
        this.context = context;
    }

    public String readFile(String fileName) {
        try {
            FileInputStream fileInputStream = context.openFileInput(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String str;
            StringBuilder stringBuilder = new StringBuilder();
            while ((str = bufferedReader.readLine()) != null) {
                stringBuilder.append(str);
            }
            fileInputStream.close();
            return stringBuilder.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void writeToFile(String fileName, String msg) {
        try {
            FileOutputStream output = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            BufferedWriter bufferedWriter = new BufferedWriter(new PrintWriter(output));
            bufferedWriter.write(msg);
            bufferedWriter.flush();
            output.close();
//            Log.i(TAG, msg);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
