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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private static final String REMOTE_FILE = "/israel-public-transportation.zip";
    private TextView helloTextView;
    private Realm realm;


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
        helloTextView = (TextView) findViewById(R.id.hello_text);
        Realm.init(this);
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
            Log.d(TAG, "now downloading file");
            String destination = getFilesDir() + REMOTE_FILE;
            FTPDownload f = new FTPDownload(server[0], REMOTE_FILE, destination);
//            success = f.retrieve(busZipFile); //TODO

            //unzip file (if download successful)
            if (success) {
                Log.d(TAG, "unzipping file");
                Decompress d = new Decompress(destination, getFilesDir() + "/unzipped/");
//                success = d.unzip();

                //build realm db (if download, unzip successful)
                //TODO
                //use stops.txt
                Log.d(TAG, "building DB");

                realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        try {
                            FileInputStream is = new FileInputStream(getFilesDir() + "/unzipped/stops.txt");
                            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                            String line;
                            line = reader.readLine(); //read away header
                            while ((line = reader.readLine()) != null) {
                                String[] rowData = line.split(",");
                                if(rowData.length == 9) {//if line is legal length, build object
                                    BusStop stop = realm.createObject(BusStop.class);
                                    Log.d(TAG, String.valueOf(rowData.length));
                                    stop.setId(Integer.parseInt(rowData[0]));
                                    stop.setCode(Integer.parseInt(rowData[1]));
                                    stop.setName(rowData[2]);
                                    stop.setDesc(rowData[3]);
                                    stop.setLat(Double.parseDouble(rowData[4]));
                                    stop.setLon(Double.parseDouble(rowData[5]));
                                    stop.setLocType(Integer.parseInt(rowData[6]));
                                    stop.setParentStation(rowData[7]);
                                    stop.setZone(rowData[8]);
                                }
                            }
                            is.close();
                        } catch (IOException e) {
                            // handle exception TODO
                            //also - are things closed properly?
                        }
                    }
                });
                realm.close();

            }


            return success;

        }

        protected void onPreExecute() {
            helloTextView.setText("Fetching data");
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                helloTextView.setText("File downloaded and unzipped");
            } else {
                helloTextView.setText("Download or unzipping failed");
            }

        }


    }

    public void downloadFile(View view) {
        String server = "gtfs.mot.gov.il";
        new getBusData().execute(server);
    }

}
