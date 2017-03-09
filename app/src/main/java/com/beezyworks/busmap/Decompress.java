package com.beezyworks.busmap;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Beezy Works Studios on 3/1/2017.
 * Copied from http://www.jondev.net/articles/Unzipping_Files_with_Android_%28Programmatically%29
 */

public class Decompress {
    private String _zipFile;
    private String _location;
    private LinkedList<String> _fileNames;

    public Decompress(String zipFile, String location) {
        _zipFile = zipFile;
        _location = location;
        _fileNames = new LinkedList<String>();
        _dirChecker("");
    }

    public boolean unzip() {
        try  {
            FileInputStream fin = new FileInputStream(_zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                Log.v("Decompress", "Unzipping " + ze.getName());

                if(ze.isDirectory()) {
                    _dirChecker(ze.getName());
                } else {
                    _fileNames.add(ze.getName()); //TODO what to do if error thrown - remove last entry on list?
                    byte[] buffer = new byte[1024];
                    int length;
                    FileOutputStream fout = new FileOutputStream(_location + ze.getName());
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

    public LinkedList<String> getFileNames(){
        return _fileNames;
    }

    private void _dirChecker(String dir) {
        File f = new File(_location + dir);

        if(!f.isDirectory()) {
            f.mkdirs();
        }
    }

}
