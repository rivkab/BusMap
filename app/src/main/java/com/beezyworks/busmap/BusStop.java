package com.beezyworks.busmap;

import android.location.Location;

import io.realm.RealmObject;

/**
 * Created by Beezy Works Studios on 3/9/2017.
 */


//Typically, though, you'd set up a server that processes the GTFS data and then makes it available to the app via a REST API.  For example, see:
//OneBusAway - https://github.com/OneBusAway/onebusaway-application-modules/wiki
//        OpenTripPlanner - http://www.opentripplanner.org/

//https://github.com/luqmaan/awesome-transit#gtfs - good resources

public class BusStop extends RealmObject {

    private int id;
    private int code;
    private String name;
    private String desc;
    private double lat;
    private double lon;

    private int locType; //note that this could be bool or enum
    private String parentStation;
    private String zone;

    public boolean nearby(int maxDistance, double sourceLat, double sourceLon){
        float[] distance = new float[1];
        Location.distanceBetween(sourceLat, sourceLon, lat, lon, distance);
        return distance[0] < maxDistance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public int getLocType() {
        return locType;
    }

    public void setLocType(int locType) {
        this.locType = locType;
    }

    public String getParentStation() {
        return parentStation;
    }

    public void setParentStation(String parentStation) {
        this.parentStation = parentStation;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }
}
