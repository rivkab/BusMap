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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = MainActivity.class.getName();
    private static final String REMOTE_FILE = "/israel-public-transportation.zip";
    private static final String SERVER = "gtfs.mot.gov.il";
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
                Snackbar.make(view, "Downloading and unpacking data file...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                new getBusData().execute(SERVER);

            }
        });
        Realm.init(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close(); // Remember to close Realm when done.
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        //TODO make this do what i actually want it to do
        //make a LatLng from each stop in DB - ideally only the nearby stops - is this possible? are they sorted?
        //Add a busstop marker for each one
        //hovering/clicking on busstop should give the other metadata
        //draw colored line for each route (may need other data file for this?)

        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        LatLng sydney = new LatLng(-33.852, 151.211);
        googleMap.addMarker(new MarkerOptions().position(sydney)
                .title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private class getBusData extends AsyncTask<String, Void, Boolean> {

        File busZipFile; //TODO can maybe declare file inside FTPDownload

        protected Boolean doInBackground(String... server) {
            boolean success = true;

            //download file
            Log.d(TAG, "now downloading file");
            String destination = getFilesDir() + REMOTE_FILE;
            FTPDownload f = new FTPDownload(server[0], REMOTE_FILE, destination);
            success = f.retrieve(busZipFile); //TODO

            //unzip file (if download successful)
            if (success) {
                Log.d(TAG, "unzipping file");
                Decompress d = new Decompress(destination, getFilesDir() + "/unzipped/");
                success = d.unzip();  //TODO
            }
            return success;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                //build realm db (if download, unzip successful) -use stops.txt
                Log.d(TAG, "building DB");
                DBBuilder dbb = new DBBuilder(getFilesDir() + "/unzipped/stops.txt");
                dbb.buildStopsDB();  //TODO error handling for this
            } else {
                Log.d(TAG, "Downloading or unzipping failed");
            }
        }
    }

}
