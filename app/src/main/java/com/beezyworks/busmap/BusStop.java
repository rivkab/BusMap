package com.beezyworks.busmap;

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

    //private LOC_TYPE locType;
    //private int parentStation;
    //private int zone;

}
