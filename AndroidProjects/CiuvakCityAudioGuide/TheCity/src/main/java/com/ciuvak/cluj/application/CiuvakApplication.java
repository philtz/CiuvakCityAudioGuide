package com.ciuvak.cluj.application;

/**
 * Created by philtz on 24-Apr-14.
 */

import android.app.Application;
import android.content.res.Resources;

import com.ciuvak.cluj.R;

import java.util.ArrayList;

import test.ciuvak.cluj.mock.TestPoiMockData;


/**
 * Class that stores global application state
 */
public class CiuvakApplication extends Application {

    private static CiuvakApplication instance;

    /**
     * application preferences
     */
    public CiuvakApplicationPreferences ciuvakApplicationPreferences;

    public ArrayList<TestPoiMockData> listTestPoiMockData;

    public static CiuvakApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        if (ciuvakApplicationPreferences == null) {
            ciuvakApplicationPreferences = new CiuvakApplicationPreferences(this);
        } else {
            ciuvakApplicationPreferences.setContext(this);
        }

        generateTestPoiMockDataList();
    }

    private void generateTestPoiMockDataList() {
        Resources appRes = instance.getResources();
        int[] poiIds = appRes.getIntArray(R.array.poi_ids);
        String[] poiNames = appRes.getStringArray(R.array.poi_names);
        String[] poiLats = appRes.getStringArray(R.array.poi_names_lats);
        String[] poiLongs = appRes.getStringArray(R.array.poi_names_longs);
        int[] poiPriorities = appRes.getIntArray(R.array.poi_names_priorities);

        listTestPoiMockData = new ArrayList<TestPoiMockData>();
        for(int i=0; i<poiIds.length; i++) {
            TestPoiMockData poiMockData = new TestPoiMockData();
            poiMockData.id = poiIds[i];
            poiMockData.name = poiNames[i];
            poiMockData.latitude = Double.valueOf(poiLats[i]);
            poiMockData.longitude = Double.valueOf(poiLongs[i]);
            poiMockData.priority = poiPriorities[i];

            listTestPoiMockData.add(poiMockData);
            poiMockData = null;
        }
    }


}
