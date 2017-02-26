package com.beezyworks.busmap;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

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
            boolean error = false;
            try {
                int reply;

                ftp.connect(server[0]);
                System.out.println("Connected to " + server + ".");
                System.out.print(ftp.getReplyString());

                // After connection attempt, you should check the reply code to verify
                // success.
                reply = ftp.getReplyCode();

                if(!FTPReply.isPositiveCompletion(reply)) {
                    ftp.disconnect();
                    System.err.println("FTP server refused connection.");
                    System.exit(1);
                }
                //... // transfer files TODO
                ftp.logout();
            } catch(IOException e) {
                error = true;
                e.printStackTrace();
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


            return Boolean.TRUE;
        }


    }

    public void downloadFile(View view){
        String server = "ftp://gtfs.mot.gov.il/israel-public-transportation.zip";
        new DownloadFilesTask().execute(server);
    }
        //String url = "ftp://gtfs.mot.gov.il/israel-public-transportation.zip";//"http://www.brainjar.com/java/host/test.html";


    /*    FTPClient ftp = new FTPClient();

        boolean error = false;
        try {
            int reply;
            String server = "ftp://gtfs.mot.gov.il/israel-public-transportation.zip";
            ftp.connect(server);
            System.out.println("Connected to " + server + ".");
            System.out.print(ftp.getReplyString());

            // After connection attempt, you should check the reply code to verify
            // success.
            reply = ftp.getReplyCode();

            if(!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                System.err.println("FTP server refused connection.");
                System.exit(1);
            }
            //... // transfer files TODO
            ftp.logout();
        } catch(IOException e) {
            error = true;
            e.printStackTrace();
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
             /*
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Israel bus gtfs");
        request.setTitle("Israel_bus_gtfs");

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        //request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "israel_gtfs.zip");

        // get download service and enqueue file
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);

        return true;
    }
    */
}
