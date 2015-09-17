/*
 * Project:  NextGIS Mobile
 * Purpose:  Mobile GIS for Android.
 * Author:   Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
 * Author:   NikitaFeodonit, nfeodonit@yandex.com
 * Author:   Stanislav Petriakov, becomeglory@gmail.com
 * *****************************************************************************
 * Copyright (c) 2012-2015. NextGIS, info@nextgis.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextgis.maplib.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;


public class FileUtil
{

    public static boolean isIntegerParseInt(String str)
    {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }


    public static void writeToFile(
            File filePath,
            String sData)
            throws IOException
    {
        FileOutputStream os = new FileOutputStream(filePath, false);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(os);
        outputStreamWriter.write(sData);
        outputStreamWriter.close();
    }


    public static String readFromFile(File filePath)
            throws IOException
    {

        String ret = "";

        FileInputStream inputStream = new FileInputStream(filePath);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String receiveString;
        StringBuilder stringBuilder = new StringBuilder();

        while ((receiveString = bufferedReader.readLine()) != null) {
            stringBuilder.append(receiveString);
        }

        inputStream.close();
        ret = stringBuilder.toString();

        return ret;
    }


    public static synchronized void createDir(File dir)
    {
        if (dir.exists()) {
            return;
        }
        if (!dir.mkdirs()) {
            throw new RuntimeException("Can not create dir " + dir);
        }
    }


    public static boolean deleteRecursive(File fileOrDirectory)
    {
        boolean isOK = true;

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                isOK = deleteRecursive(child) && isOK;
            }
        }

        return fileOrDirectory.delete() && isOK;
    }


    public static boolean move(
            File from,
            File to)
    {
        return copyRecursive(from, to) && deleteRecursive(from);
    }


    public static boolean copyRecursive(
            File from,
            File to)
    {
        if (from.isDirectory()) {
            if (!to.exists()) {
                if (!to.mkdir()) {
                    return false;
                }
            }

            for (String path : from.list()) {
                if (!copyRecursive(new File(from, path), new File(to, path))) {
                    return false;
                }
            }
        } else {

            try {
                InputStream in = new FileInputStream(from);
                OutputStream out = new FileOutputStream(to);
                byte[] buf = new byte[Constants.IO_BUFFER_SIZE];
                copyStream(in, out, buf, Constants.IO_BUFFER_SIZE);
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }


    public static void copyStream(
            InputStream is,
            OutputStream os,
            byte[] buffer,
            int bufferSize)
            throws IOException
    {
        int len;
        while ((len = is.read(buffer, 0, bufferSize)) > 0) {
            os.write(buffer, 0, len);
        }
    }


    public static String getFileNameByUri(
            final Context context,
            Uri uri,
            String defaultName)
    {
        String fileName = defaultName;
        try {
            if (uri.getScheme().compareTo("content") == 0) {
                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                if (null != cursor) {
                    try {
                        if (cursor.moveToFirst()) {
                            int column_index =
                                    cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                            fileName = cursor.getString(column_index);
                        }
                    } catch (Exception e) {
                        //Log.d(TAG, e.getLocalizedMessage());
                    } finally {
                        cursor.close();
                    }
                }
            } else if (uri.getScheme().compareTo("file") == 0) {
                fileName = uri.getLastPathSegment();
            } else {
                fileName = fileName + "_" + uri.getLastPathSegment();
            }
        } catch (Exception e) {
            //do nothing, only return default file name;
            Log.d(Constants.TAG, e.getLocalizedMessage());
        }
        return fileName;
    }


    public static String removeExtension(String filePath)
    {
        // These first few lines the same as Justin's
        File f = new File(filePath);

        // if it's a directory, don't remove the extension
        if (f.isDirectory()) {
            return filePath;
        }

        String name = f.getName();

        // Now we know it's a file - don't need to do any special hidden
        // checking or contains() checking because of:
        final int lastPeriodPos = name.lastIndexOf('.');
        if (lastPeriodPos <= 0) {
            // No period after first character - return name as it was passed in
            return filePath;
        } else {
            // Remove the last period and everything after it
            File renamed = new File(f.getParent(), name.substring(0, lastPeriodPos));
            return renamed.getPath();
        }
    }


    public static String getExtension(String filePath)
    {
        File f = new File(filePath);
        if (f.isDirectory()) {
            return "";
        }

        String name = f.getName();

        final int lastPeriodPos = name.lastIndexOf('.');
        if (lastPeriodPos <= 0) {
            return "";
        } else {
            return name.substring(lastPeriodPos + 1, name.length());
        }
    }


    public static long getDirectorySize(File dir)
    {
        long size = 0;
        File[] files = dir.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                size += file.length();
            } else {
                size += getDirectorySize(file);
            }
        }

        return size;
    }


    public static boolean isDirectoryWritable(File directory)
    {
        File toCreate = new File(directory, "hello");
        try {
            toCreate.createNewFile();
            return true;
        } catch (IOException e) {
            // It's expected we'll get a "Permission denied" exception.
        } finally {
            toCreate.delete();
        }
        return false;
    }
}
