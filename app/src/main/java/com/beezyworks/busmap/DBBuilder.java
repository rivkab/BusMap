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

public class DBBuilder {

    private Realm realm;
    private String filePath;

    public DBBuilder(String filePath){
        this.filePath = filePath;
    }

    public void buildStopsDB(){

        realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(new Realm.Transaction() {

            @Override
            public void execute(Realm realm) {

                FileInputStream is = null;
                try {
                    is = new FileInputStream(filePath); //getFilesDir() + "/unzipped/stops.txt"
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String line = reader.readLine(); //read away header line
                    while ((line = reader.readLine()) != null) {
                        String[] rowData = line.split(",");
                        if(rowData.length == 9) {//if line is legal length, build object
                            BusStop stop = realm.createObject(BusStop.class);
                            stop.setId(Integer.parseInt(rowData[0]));  //TODO this is for a SPECIFIC file. messy
                            stop.setCode(Integer.parseInt(rowData[1]));
                            stop.setName(rowData[2]);
                            stop.setDesc(rowData[3]);
                            stop.setLat(Double.parseDouble(rowData[4]));
                            stop.setLon(Double.parseDouble(rowData[5]));
                            stop.setLocType(Integer.parseInt(rowData[6]));
                            stop.setParentStation(rowData[7]);
                            stop.setZone(rowData[8]);
                        }
                    }
                    is.close();
                    Log.d("DBBuilder", "DB built");
                } catch (IOException e) {
                    // handle exception TODO
                    Log.e("DBBuilder","error building DB");

                }finally {
                    IOUtils.closeQuietly(is);
                }

            }

        });
        realm.close();

    }
}
