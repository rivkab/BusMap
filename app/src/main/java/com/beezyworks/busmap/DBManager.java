package com.beezyworks.busmap;

import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
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
        busStopsFilePath = rootPath+ "/unzipped/stops.txt";
        tripsFilePath = rootPath+"/unzipped/stop_times.txt"; //TODO check
    }

    private interface LineParser {
        public void parseLine(String line);
    }

    private class BusStopLineParser implements LineParser {
        final private int legalLength = 9;

        @Override
        public void parseLine(String line) {
            String[] rowData = line.split(",");
            if(rowData.length == legalLength) {//if line is legal length, build object
                BusStop stop = realm.createObject(BusStop.class);
                stop.setId(Integer.parseInt(rowData[0]));  //TODO this is for a SPECIFIC file. messy
                stop.setName(rowData[2]);
                stop.setLat(Double.parseDouble(rowData[4]));
                stop.setLon(Double.parseDouble(rowData[5]));

            }
        }
    }

    private class TripLineParser implements LineParser {

        final private int legalLength = 8;

        @Override
        public void parseLine(String line) {
            //TODO
        }
    }

    private boolean parseFile(String filePath, LineParser lineParser){
        boolean success = true;
        FileInputStream is = null;
        try {
            is = new FileInputStream(filePath); //getFilesDir() + "/unzipped/stops.txt"
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine(); //read away header line
            while ((line = reader.readLine()) != null) {
                lineParser.parseLine(line);
            }
            is.close();
            Log.d("DBManager", "DB built");
        } catch (IOException e) {
            success = false;
            Log.e("DBManager","error building DB");

        }finally {
            IOUtils.closeQuietly(is);
        }
        return success;
    }

    public void buildDB(){
        realm = Realm.getDefaultInstance();

        realm.executeTransactionAsync(new Realm.Transaction() {

            @Override
            public void execute(Realm realm) {

                boolean success = parseFile(busStopsFilePath, new BusStopLineParser());
                if(success){
                    parseFile(tripsFilePath, new TripLineParser());
                }

            }

        });
        realm.close();

    }




}
