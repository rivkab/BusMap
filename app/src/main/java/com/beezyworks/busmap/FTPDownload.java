package com.beezyworks.busmap;

import android.util.Log;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static android.content.ContentValues.TAG;

/**
 * Created by Beezy Works Studios on 3/2/2017.
 */

public class FTPDownload {


    private String _FTPserver;
    private String _serverFilePath;
    private String _destinationPath;

    public FTPDownload(String FTPserver, String serverFilePath, String destinationPath){
        _FTPserver = FTPserver;
        _serverFilePath = serverFilePath;
        _destinationPath = destinationPath;
    }

    public boolean retrieve(File destination){
        FTPClient ftp = new FTPClient();
        boolean success = true;
        try {
            //connect to ftp server
            int reply;
            ftp.connect(_FTPserver);
            ftp.login("anonymous", "me@gmail.com");

            // After connection attempt, check the reply code to verify success.
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                Log.e(TAG, "FTP server refused connection.");
                return false;
            }

            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);


            //transfer files
            destination = new File(_destinationPath);
            OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(destination));
            boolean gotFile = ftp.retrieveFile(_serverFilePath, outputStream1);
            //String whatIs = ftp.getReplyString();
            outputStream1.close();
            if (!gotFile) {
                success = false;
            }

            ftp.logout();
        } catch (IOException e) {
            success = false;
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                    Log.d("IO Exception", ioe.getMessage());
                }
            }
        }
        return success;
    }

}
