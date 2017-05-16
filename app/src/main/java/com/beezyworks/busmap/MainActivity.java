package com.beezyworks.busmap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
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
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

import static com.beezyworks.busmap.R.id.map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationHelper.LocationHandled{

    private static final String TAG = MainActivity.class.getName();
    private static final String REMOTE_FILE = "/israel-public-transportation.zip";
    private static final String SERVER = "gtfs.mot.gov.il";
    private static final int MAXDISTANCE = 1000; //max distance from current loc to show on map. in meters
    private static final int LAUNCH_ZOOM = 8;
    private static final int VIEW_ZOOM = 17;
    private Realm realm;
    private LocationHelper locationHelper;
    private double currentLat = 31.7683;
    private double currentLon = 35.2137;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationHelper = new LocationHelper(this, this);
        locationHelper.connect();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Downloading and unpacking data file...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                new getBusData().execute(SERVER);//TODO when to do this? also does it run when screen is off?

            }
        });
        initRealm();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
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
    protected void onPause() {
        super.onPause();
        locationHelper.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationHelper.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close(); // Remember to close Realm when done.
    }

    private void initRealm() {
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        //TODO make this do what i actually want it to do
        //make a LatLng of each nearby stop
        //Add a busstop marker for each one
        //hovering/clicking on busstop should give the other metadata
        //draw colored line for each route (may need other data file for this?)

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLat, currentLon), LAUNCH_ZOOM));
        new setStopMarkers().execute(googleMap);

    }

    @Override
    public void locationAvailable(Location location, int locationSource) {
        currentLat = location.getLatitude();
        currentLon = location.getLongitude();
        Log.i(TAG, "Location gotten! "+location.getLatitude()+" "+location.getLongitude());
        locationHelper.disconnect();
    }


    //go over realm db and make marker for each latlng
    private class setStopMarkers extends  AsyncTask<GoogleMap, Void, GoogleMap>{

        ArrayList<simpleStop> stops;

        private class simpleStop{
            private LatLng coordinates;
            private String name;

            public simpleStop(double lat, double lon, String nm){
                coordinates = new LatLng(lat,lon);
                name = nm;
            }
            protected LatLng getCoordinates(){ return coordinates; }
            protected String getDescription(){ return name; }
        }

        protected GoogleMap doInBackground(GoogleMap... map){
            Log.d(TAG, "adding markers");
            //TODO should we redo this as zoom changes?
            stops = new ArrayList<>();
            realm = Realm.getDefaultInstance();
            RealmResults<BusStop> results = realm.where(BusStop.class).findAll();
            for (BusStop b : results) {
                if(b.nearby(MAXDISTANCE,currentLat, currentLon)) {  //only get nearby stops
                    stops.add(new simpleStop(b.getLat(), b.getLon(), b.getName()));
                }
            }
            realm.close();
            return map[0];
        }

        protected void onPostExecute(GoogleMap map){
            for(simpleStop l : stops){
                map.addMarker(new MarkerOptions().position(l.getCoordinates()).title(l.getDescription()));
            }
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLat, currentLon), VIEW_ZOOM));
            Log.d(TAG, "markers added");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LocationHelper.LOCATION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                       locationHelper.connect();
                    } else {
                        // Permission denied
                        // TODO: kick them out of the app?
                    }
                }
            }
        }
    }

    private class getBusData extends AsyncTask<String, Void, Boolean> {

        //TODO check if file is there; if so, check if timestamp is recent enough to not need download
        File busZipFile; //TODO can maybe declare file inside FTPDownload
        ArrayList<String> desiredFiles = new ArrayList<>(); //TODO this is messy

        protected Boolean doInBackground(String... server) {
            boolean success = true;

            //download file
//            Log.d(TAG, "now downloading file");
//            String destination = getFilesDir() + REMOTE_FILE;
//            FTPDownload f = new FTPDownload(server[0], REMOTE_FILE, destination);
//            success = f.retrieve(busZipFile); //TODO
//
//            //unzip file (if download successful)
//            if (success) {
//                Log.d(TAG, "unzipping file");
//                desiredFiles.add("stops.txt");//TODO messy
//                Decompress d = new Decompress(destination, getFilesDir() + "/unzipped/");
//                success = d.unzip(desiredFiles);  //TODO
//            }
            return success;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                //build realm db (if download, unzip successful) -use stops.txt
                Log.d(TAG, "building DB");
                DBManager dbm = new DBManager(getFilesDir().getPath());
                dbm.buildDB();  //TODO error handling for this
            } else {
                Log.d(TAG, "Downloading or unzipping failed");
            }
        }

    }

}
