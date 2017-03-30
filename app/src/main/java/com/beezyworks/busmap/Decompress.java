package com.beezyworks.busmap;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Beezy Works Studios on 3/1/2017.
 * Copied from http://www.jondev.net/articles/Unzipping_Files_with_Android_%28Programmatically%29
 */

public class Decompress {
    private String zipFile;
    private String location;

    public Decompress(String zipFile, String location) {
        this.zipFile = zipFile;
        this.location = location;
        dirChecker("");
    }

    public boolean unzip(ArrayList<String> desiredFiles) {
        try  {
            FileInputStream fin = new FileInputStream(zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                if(ze.isDirectory()) {
                    dirChecker(ze.getName());
                } else if(desiredFiles.contains(ze.getName())){
                    Log.v("Decompress", "Unzipping " + ze.getName());
                    byte[] buffer = new byte[2048];
                    int length;
                    FileOutputStream fout = new FileOutputStream(location + ze.getName());
                    while ((length = zin.read(buffer))>0) {
                        fout.write(buffer, 0, length);
                    }

                    zin.closeEntry();
                    fout.close();
                }

            }
            zin.close();
            return true;
        } catch(Exception e) {
            Log.e("Decompress", "unzip", e);
            return false;
        }

    }

    private void dirChecker(String dir) {
        File f = new File(location + dir);

        if(!f.isDirectory()) {
            f.mkdirs();
        }
    }

}
