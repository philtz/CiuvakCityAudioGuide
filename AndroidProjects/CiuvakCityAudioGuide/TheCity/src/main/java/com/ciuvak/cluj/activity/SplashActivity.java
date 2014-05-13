package com.ciuvak.cluj.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.ciuvak.cluj.R;
import com.ciuvak.cluj.application.CiuvakApplication;
import com.skobbler.ngx.SKMaps;
import com.skobbler.ngx.SKMapsInitSettings;
import com.skobbler.ngx.SKPrepareMapTextureListener;
import com.skobbler.ngx.SKPrepareMapTextureThread;
import com.skobbler.ngx.map.SKMapViewStyle;
import com.skobbler.ngx.navigation.SKAdvisorSettings;

import java.io.File;

/**
 * Created by philtz on 24-Apr-14.
 */
public class SplashActivity extends BaseActivity implements SKPrepareMapTextureListener {

    private static final String API_KEY = "4130e27825b5924bfb3f1357e41aaf9c70dacf2e721771bd1d5b2738b1abac26";

    /**
     * Path to the MapResources directory
     */
    private String mapResourcesDirPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        File externalDir = getExternalFilesDir(null);

        // determine path where map resources should be copied on the device
        if (externalDir != null) {
            mapResourcesDirPath = externalDir + "/" + "SKMaps/";
        } else {
            mapResourcesDirPath = getFilesDir() + "/" + "SKMaps/";
        }

        if (!new File(mapResourcesDirPath).exists()) {
            // if map resources are not already present copy them to
            // mapResourcesDirPath in the following thread
            new SKPrepareMapTextureThread(this, mapResourcesDirPath, "SKMaps.zip", this).start();
        } else {
            // map resources have already been copied - start the map activity
            initializeLibrary();
            mockupTimer();
        }
    }

    /**
     * Initializes the SKMaps framework
     */
    private void initializeLibrary() {
        // get object holding map initialization settings
        SKMapsInitSettings initMapSettings = new SKMapsInitSettings();
        // set path to map resources and initial map style
        initMapSettings.setMapResourcesPaths(mapResourcesDirPath,
                new SKMapViewStyle(mapResourcesDirPath + "daystyle/", "daystyle.json"));

        SKMaps.getInstance().initializeSKMaps(this, initMapSettings, API_KEY);
    }

    /**
     * Used to pass to the next activity after 3 sec.
     */
    private void mockupTimer() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                goToMainActivity();
            }
        }, 3000);
    }

    @Override
    public void onMapTexturesPrepared(boolean b) {
        initializeLibrary();
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(SplashActivity.this, "Map resources were copied", Toast.LENGTH_SHORT).show();
                goToMainActivity();
            }
        });
    }

    private void goToMainActivity() {
        Intent mainActivityIntent = new Intent(SplashActivity.this, MapActivity.class);
        startActivity(mainActivityIntent);
        finish();
    }
}
