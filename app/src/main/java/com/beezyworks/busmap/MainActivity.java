package com.beezyworks.busmap;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String REMOTE_FILE = "israel-public-transportation.zip";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class DownloadFilesTask extends AsyncTask<String, Void, Boolean> {


        TextView helloTextView = (TextView)findViewById(R.id.hello_text);
        File downloadFile;

        protected Boolean doInBackground(String... server) {

            FTPClient ftp = new FTPClient();
            boolean success = true;
            try {
                //connect to ftp server
                int reply;
                ftp.connect(server[0]);
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
                downloadFile = new File(getFilesDir(), "/"+REMOTE_FILE);
                OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile));
                boolean gotFile = ftp.retrieveFile(REMOTE_FILE, outputStream1);
                //String whatIs = ftp.getReplyString();
                outputStream1.close();

                if (gotFile) {
                    Log.d(TAG,"File has been downloaded successfully.");
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

        protected void onPreExecute(){
            helloTextView.setText("Downloading...");
        }

        //unpack zip. also convert to JSON?
        protected void onPostExecute(Boolean result){
            //TODO
            helloTextView.setText("File has been downloaded successfully.");
            File internal[] = getFilesDir().listFiles();
            for (File f: internal){
                Log.d(TAG,f.getName());

            }

            Decompress d = new Decompress(getFilesDir()+"/"+REMOTE_FILE, getFilesDir()+"/unzipped/");
            d.unzip();


        }
    }

    public void downloadFile(View view) {
        String server = "gtfs.mot.gov.il";
        new DownloadFilesTask().execute(server);
    }

}
