package com.beezyworks.busmap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationHelper implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    GoogleApiClient googleApiClient;
    Context context;
    LocationHandled locationHandled;


    public static final int LOCATION_REQUEST_CODE = 99;
    public static final int SAVED = 0;
    public static final int RETRIEVED = 1;

    public LocationHelper(Context context, LocationHandled locationHandled) {
        this.context = context;
        this.locationHandled = locationHandled;
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void connect() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS)
                googleApiClient.connect();
        } else {
            if (context instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
            disconnect();
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(10);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {

//        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
//        Resources res = context.getResources();
//        editor.putString(res.getString(R.string.longitudeKey), String.valueOf(location.getLongitude()));
//        editor.putString(res.getString(R.string.latitudeKey), String.valueOf(location.getLatitude()));
//        editor.putString(res.getString(R.string.elevationKey), String.valueOf(location.getAltitude()));
//        editor.apply();
        locationHandled.locationAvailable(location, RETRIEVED);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        Resources res = context.getResources();
//        double longitude = Double.valueOf(sharedPreferences.getString(res.getString(R.string.longitudeKey), "35.235806"));
//        double latitude = Double.valueOf(sharedPreferences.getString(res.getString(R.string.latitudeKey), "31.777972"));
//        double elevation = Double.valueOf(sharedPreferences.getString(res.getString(R.string.elevationKey), "0"));
//        if (elevation <= 0d)
//            elevation = 1d;
//        Location location = new Location(LocationManager.GPS_PROVIDER);
//        location.setLatitude(latitude);
//        location.setLongitude(longitude);
//        location.setAltitude(elevation);
//        locationHandled.locationAvailable(location, SAVED);
        disconnect();
    }

    public interface LocationHandled {
        void locationAvailable(Location location, int locationSource);

    }

    public void disconnect() {
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
        googleApiClient.disconnect();
    }
}