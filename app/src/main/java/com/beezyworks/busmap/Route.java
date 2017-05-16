package com.beezyworks.busmap;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Beezy Works Studios on 5/16/2017.
 */

public class Route extends RealmObject {

    private short busNumber;
    private String name;
    private RealmList<BusStop> stops;

    public short getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(short busNumber) {
        this.busNumber = busNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RealmList<BusStop> getStops() {
        return stops;
    }

    public void setStops(RealmList<BusStop> stops) {
        this.stops = stops;
    }
}
