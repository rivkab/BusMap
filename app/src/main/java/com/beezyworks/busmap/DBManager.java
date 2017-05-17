package com.beezyworks.busmap;

import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import io.realm.Realm;


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
                BusStop stop = currRealm.createObject(BusStop.class);//TODO crashing the app
                stop.setId(Integer.parseInt(lineData[0]));
                stop.setName(lineData[2]);
                stop.setLat(Double.parseDouble(lineData[4]));
                stop.setLon(Double.parseDouble(lineData[5]));

            }
        }
    }

    private class TripLineParser implements LineParser {

        final private int legalLength = 8;
        private int currTripId = 0;
        private Realm currRealm;

        public TripLineParser(Realm currRealm){
            this.currRealm = currRealm;
        }

        @Override
        public void parseLine(String[] lineData) {
            //TODO

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

                boolean success = parseFile(busStopsFilePath, new BusStopLineParser(asyncRealm));
                if(success){
                    parseFile(tripsFilePath, new TripLineParser(asyncRealm));
                    Log.d("DBManager", "DB built");
                }

            }
        });


    }




}
