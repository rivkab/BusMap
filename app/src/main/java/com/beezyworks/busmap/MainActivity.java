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

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private static final String REMOTE_FILE = "/israel-public-transportation.zip";
    private TextView helloTextView;


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
        helloTextView = (TextView)findViewById(R.id.hello_text);
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


    private class getBusData extends AsyncTask<String, Void, Boolean> {

        File busZipFile; //TODO can maybe declare file inside FTPDownload

        protected Boolean doInBackground(String... server) {

            boolean success = true;

            //download file
            Log.d(TAG,"now downloading file");
            String destination = getFilesDir()+REMOTE_FILE;
            FTPDownload f = new FTPDownload(server[0],REMOTE_FILE,destination);
//            success = f.retrieve(busZipFile); //TODO

            //unzip file (if download successful)
            if(success){
                Log.d(TAG,"unzipping file");
                Decompress d = new Decompress(destination, getFilesDir()+"/unzipped/");
                success = d.unzip();

                //build realm db (if download, unzip successful)
                //TODO
                //use stops.txt
                Log.d(TAG,"building DB");

            }



            return success;

        }

        protected void onPreExecute(){
            helloTextView.setText("Fetching data");
        }

        protected void onPostExecute(Boolean result){
            if(result) {
                helloTextView.setText("File downloaded and unzipped");
            }else{
                helloTextView.setText("Download or zipping failed");
            }

        }


    }

    public void downloadFile(View view) {
        String server = "gtfs.mot.gov.il";
        new getBusData().execute(server);
    }

}
