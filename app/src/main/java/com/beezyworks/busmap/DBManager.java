package com.beezyworks.busmap;

import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import io.realm.Realm;
import io.realm.RealmList;


/**
 * Created by Beezy Works Studios on 3/14/2017.
 */

public class DBManager {

    private Realm realm;
    private String busStopsFilePath;
    private String tripsFilePath;

    public DBManager(String rootPath){
        busStopsFilePath = rootPath + "/unzipped/stops.txt";
        tripsFilePath = rootPath + "/unzipped/stop_times.txt"; //TODO check
    }

    private interface LineParser {
        void parseLine(String[] lineData);
    }

    private class BusStopLineParser implements LineParser {
        final private int legalLength = 9;
        Realm currRealm;

        public BusStopLineParser(Realm currRealm){
            this.currRealm = currRealm;
        }

        @Override
        public void parseLine(String[] lineData) {
            if(lineData.length == legalLength) {//if line is legal length, build object
                BusStop stop = currRealm.createObject(BusStop.class,Integer.parseInt(lineData[0]) );
               // stop.setId(Integer.parseInt(lineData[0]));
                stop.setName(lineData[2]);
                stop.setLat(Double.parseDouble(lineData[4]));
                stop.setLon(Double.parseDouble(lineData[5]));

            }
        }
    }

    private class TripLineParser implements LineParser {

        final private int legalLength = 8;
        private String currTripId = "";
        private Realm currRealm;

        public TripLineParser(Realm currRealm){
            this.currRealm = currRealm;
        }

        @Override
        public void parseLine(String[] lineData) {
            String id = lineData[0];
            //if trip doesn't yet exist, build new trip
            Trip trip = currRealm.where(Trip.class).equalTo("id", id).findFirst();
            if (trip == null) {
                trip = currRealm.createObject(Trip.class, id);
                //trip.setBusNumber(); TODO
                //trip.setName(); TODO
                trip.setStops(new RealmList<BusStop>());//TODO is this correct
            }
            //add stop to trip's list - find it in realm using stop id
            trip.getStops().add(currRealm.where(BusStop.class).equalTo("id",Integer.parseInt(lineData[3])).findFirst());
        }
    }

    private boolean parseFile(String filePath, LineParser lineParser){
        boolean success = true;
        FileInputStream is = null;
        try {
            is = new FileInputStream(filePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine(); //read away header line
            while ((line = reader.readLine()) != null) {
                String[] lineData = line.split(",");
                lineParser.parseLine(lineData);
            }
            is.close();
        } catch (Exception e) {
            success = false;
            Log.e("DBManager","error building DB"+e.getMessage());

        }finally {
            IOUtils.closeQuietly(is);
        }
        return success;
    }

    public void buildDB(){
        realm = Realm.getDefaultInstance();

        realm.executeTransactionAsync(new Realm.Transaction() {

            @Override
            public void execute(Realm asyncRealm) {

                long time1 = System.currentTimeMillis();//TODO
                boolean success = parseFile(busStopsFilePath, new BusStopLineParser(asyncRealm));
                long time2 = System.currentTimeMillis();//TODO
                Log.d("DBManager","Parse stops: "+(time2-time1));
                if(success){
                    parseFile(tripsFilePath, new TripLineParser(asyncRealm));
                    Log.d("DBManager", "DB built");
                    long time3 = System.currentTimeMillis();//TODO
                    Log.d("DBManager","Parse stops: "+(time2-time1)+" Parse stop_times:"+(time3-time2));
                }
            }
        });


    }




}
