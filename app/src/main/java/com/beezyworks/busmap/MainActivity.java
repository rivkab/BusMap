package com.beezyworks.busmap;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = MainActivity.class.getName();
    private static final String REMOTE_FILE = "/israel-public-transportation.zip";
    private static final String SERVER = "gtfs.mot.gov.il";
    private Realm realm;

    //Define a request code to send to Google Play services
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

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
        Realm.init(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Now lets connect to the API
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");

        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }


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


        new setStopMarkers().execute(googleMap);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {//TODO ask for runtime permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {
            //If everything went fine lets get latitude and longitude
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();

            Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        //TODO
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        /*
             * Google Play services can resolve some errors it detects.
             * If the error has a resolution, try sending an Intent to
             * start a Google Play services activity that can resolve
             * error.
             */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                    /*
                     * Thrown if Google Play services canceled the original
                     * PendingIntent
                     */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
                /*
                 * If no resolution is available, display a dialog to the
                 * user with the error.
                 */
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
    }

    //go over realm db and make marker for each latlng
    private class setStopMarkers extends  AsyncTask<GoogleMap, Void, GoogleMap>{

        ArrayList<simpleStop> stops;

        private class simpleStop{
            protected LatLng coordinates;
            protected String description;

            public simpleStop(LatLng coor, String desc){
                coordinates = coor;
                description = desc;
            }
            protected LatLng getCoordinates(){ return coordinates; }
            protected String getDescription(){ return description; }
        }

        protected GoogleMap doInBackground(GoogleMap... map){
            Log.d(TAG, "adding markers");

            stops = new ArrayList<>();
            realm = Realm.getDefaultInstance();
            RealmResults<BusStop> results = realm.where(BusStop.class).findAll(); //TODO get only nearby stops
            for (BusStop b : results) {
                LatLng stop = new LatLng(b.getLat(), b.getLon());
                stops.add(new simpleStop(stop, b.getDesc()));
            }
            realm.close();
            return map[0];
        }

        protected void onPostExecute(GoogleMap map){
//            for(LatLng l : stops){
//                map.addMarker(new MarkerOptions().position(l.getCoordinates()));
//            }

            //TODO below is a trial of one stop
            map.addMarker(new MarkerOptions().position(stops.get(0).getCoordinates())
                    .title(stops.get(0).getDescription()));
            map.moveCamera(CameraUpdateFactory.newLatLng(stops.get(0).getCoordinates()));
            Log.d(TAG, "markers added");
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
                DBManager dbm = new DBManager(getFilesDir() + "/unzipped/stops.txt");
                dbm.buildStopsDB();  //TODO error handling for this
            } else {
                Log.d(TAG, "Downloading or unzipping failed");
            }
        }
    }

}
