package com.beezyworks.busmap;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Beezy Works Studios on 5/16/2017.
 */

public class Trip extends RealmObject {



    @PrimaryKey
    private String id;
    private short busNumber;
    private String name;
    private RealmList<BusStop> stops;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
