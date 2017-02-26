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

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class DownloadFilesTask extends AsyncTask<String, Void, Boolean>{
        protected Boolean doInBackground(String... server) {

            FTPClient ftp = new FTPClient();
            boolean success = true;
            try {
                int reply;
                Log.d(TAG,"Trying to connect");
                ftp.connect(server[0]);
                Log.d(TAG,"Connected to " + server[0] + ".");

                Log.d(TAG,"Reply string: " + ftp.getReplyString());

                // After connection attempt, you should check the reply code to verify
                // success.
                reply = ftp.getReplyCode();

                if(!FTPReply.isPositiveCompletion(reply)) {
                    ftp.disconnect();
                    Log.e(TAG,"FTP server refused connection.");
                    System.exit(1);
                }
                //... // transfer files TODO
                String remoteFile = "/israel-public-transportation.zip";
                File downloadFile = new File(getFilesDir(), "/israel-public-transportation.zip");
                OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile));
                boolean gotFile = ftp.retrieveFile(remoteFile, outputStream1);
                Log.d(TAG,"Retrieved file?: "+gotFile);
                outputStream1.close();

                if (gotFile) {
                    Log.d(TAG,"File has been downloaded successfully.");
                }
                ///until here TODO



                ftp.logout();
            } catch(IOException e) {
                success = false;
                e.printStackTrace();
                Log.d(TAG,e.getMessage());
            } finally {
                if(ftp.isConnected()) {
                    try {
                        ftp.disconnect();
                    } catch(IOException ioe) {
                        // do nothing
                    }
                }
                //System.exit(error ? 1 : 0);
            }


            return success;
        }


    }

    public void downloadFile(View view){
        Log.d("myapp","hello");
        String server = "gtfs.mot.gov.il";
        new DownloadFilesTask().execute(server);
    }
        //String url = "ftp://gtfs.mot.gov.il/israel-public-transportation.zip";//"http://www.brainjar.com/java/host/test.html";



}
