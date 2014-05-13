/*
 * Copyright (c) 2011 SKOBBLER SRL.
 * Cuza Voda 1, Cluj-Napoca, Cluj, 400107, Romania
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SKOBBLER SRL 
 * ("Confidential Information"). You shall not disclose such Confidential 
 * Information and shall use it only in accordance with the terms of the license 
 * agreement you entered into with SKOBBLER SRL.
 * 
 * Created on Jan 9, 2012 by CatalinM
 * Modified on $Date$ 
 *          by $Author$
 */
package com.ciuvak.cluj.location;


import java.text.DecimalFormat;
import java.util.List;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

import com.ciuvak.cluj.activity.BaseActivity;
import com.ciuvak.cluj.activity.MapActivity;
import com.skobbler.ngx.positioner.SKPosition;

//IFDEF_NOT_NAVIGATION


//ENDEF_NOT_NAVIGATION


/**
 * Location service that detects the current position using the GPS, NETWORK and
 * PASSIVE providers
 * @author Filip Tudic
 * @version $Revision$
 */
class LocationManagerService extends LocationProviderController implements LocationListener {

    /**
     * Current best location estimate
     */
    private static Location currentBestLocation;

    /**
     * Shows whether the currentBestLocation came from an actual fix or not
     */
    private static boolean currentBestLocationType;

    /**
     * The location manager
     */
    private LocationManager locationManager;

    /**
     * Send network location to navi mode
     */
    private boolean sendNonGpsPositionsToNavi = false;

    /**
     * Timer for switching the location provider in case of disconnected
     */
    private CountDownTimer gpsDisconnectedCountDownTimer;

    public LocationManagerService(Context ctx) {
        this.locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        logToFile("LocationManagerService created!");
    }

    @Override
    public void connectLocationService(boolean initPeriodicUpdates) {
        if (initPeriodicUpdates) {
            periodicUpdatesRequested = false;
        } else {
            ACTIVE_LOCATION = false;
            gotLocation(getLastLocation());
        }
    }

    @Override
    public void disconnectLocationService() {
        if (periodicUpdatesRequested) {
            stopPeriodicUpdates();
        }
    }

    @Override
    public Location getLastLocation() {
        List<String> providers = locationManager.getAllProviders();
        for (String s : providers) {
            Location lastKnown = locationManager.getLastKnownLocation(s);
            if (lastKnown != null) {
                return lastKnown;
            }
        }
        return null;
    }

    @Override
    public void startPeriodicUpdates() {
        startLocationUpdates();
    }

    @Override
    public void stopPeriodicUpdates() {
        periodicUpdatesRequested = false;
        locationManager.removeUpdates(this);
    }

    /**
     * request the latest position to be sent as an update, do nothing if no
     * position is available
     */
    public void requestUpdateFromLastPosition() {
        if (currentBestLocation != null) {
            SKPosition currentPosition =
                    new SKPosition(currentBestLocation.getLatitude(), currentBestLocation.getLongitude());
            currentPosition.setHorizontalAccuracy(currentBestLocation.getAccuracy());
            currentPosition.setHeading(currentBestLocation.getBearing());
            currentPosition.setSpeed(currentBestLocation.getSpeed());
            currentPosition.setAltitude(currentBestLocation.getAltitude());
            if (BaseActivity.currentActivity != null) {
                if ((BaseActivity.currentActivity instanceof MapActivity)) {
                    ((MapActivity) BaseActivity.currentActivity).updateLocation(currentPosition);
                }
            }
        }
    }

    /**
     * Verifies if the settings are OK and start the all listener if not started
     */
    private void startLocationUpdates() {
        logToFile("LocationManagerService startLocationUpdates");
        periodicUpdatesRequested = true;
        List<String> providers = locationManager.getAllProviders();
        for (String s : providers) {
            locationManager.requestLocationUpdates(s, 1000, 0, this);
            Location lastKnown = locationManager.getLastKnownLocation(s);
            if (lastKnown != null) {
                gotLocation(lastKnown);
            }
        }
    }

    /**
     * Handle updates from listeners(and the last known good location) and
     * provide optimization
     * @param location location received
     */
    private void gotLocation(Location location) {
        if (location == null) {
            return;
        }
        if (gpsDisconnectedCountDownTimer != null) {
            gpsDisconnectedCountDownTimer.cancel();
            gpsDisconnectedCountDownTimer = null;
        }
        updatedTime = (String) DateFormat.format("dd-MM hh:mm:ss", System.currentTimeMillis());
//        if (ForeverMapUtils.isAppInDebugMode()) {
//            final TextView debugInfoTV = (TextView) BaseActivity.currentActivity.findViewById(R.id
//                    .debugging_info_real_gps_position);
//            if (debugInfoTV != null) {
//                if ((BaseActivity.currentActivity instanceof MapWorkflowActivity) &&
//                        ((ForeverMapApplication) (BaseActivity.currentActivity.getApplication()))
//                                .getApplicationPreferences().getBooleanPreference(PreferenceTypes
//                                .DEBUG_REAL_POSITION)) {
//                    DecimalFormat df = new DecimalFormat("#.000000");
//                    final StringBuilder debugInfo = new StringBuilder();
//                    debugInfoTV.setVisibility(View.VISIBLE);
//                    debugInfo.append("REAL CLASSIC GPS POSITION:\n Lat: ").append(df.format(location.getLatitude()))
//                            .append("; ").append(" Lon: ")
//                            .append(df.format(location.getLongitude())).append("\n Time ")
//                            .append(LocationGoogleService.updatedTime).append("\n");
//                    debugInfoTV.setText(debugInfo.toString());
//                } else {
//                    debugInfoTV.setVisibility(View.GONE);
//                }
//            }
//            logToFile("RECEIVED OLD: Long --" + location.getLongitude() + "-- Lat --" + location.getLatitude() + "-- " +
//                    "Acc --"
//                    + location.getAccuracy() + "-- Provide: --" + location.getProvider() + "-- Timestamp --"
//                    + DateFormat.format("dd-MM hh:mm:ss", System.currentTimeMillis()) + "-- Location Time: --"
//                    + DateFormat.format("dd-MM hh:mm:ss", location.getTime()) + "-- Active: --" + ACTIVE_LOCATION +
//                    "--");
//        }
        currentBestLocation = optimize(location, ACTIVE_LOCATION);
        if (currentBestLocation == null) {
            return;
        }
//        if (ForeverMapUtils.isAppInDebugMode()) {
//            logToFile("FORWARDED: Long --" + currentBestLocation.getLongitude() + "-- Lat --"
//                    + currentBestLocation.getLatitude() + "-- Acc --" + currentBestLocation.getAccuracy()
//                    + "-- Provide: --" + currentBestLocation.getProvider() + "-- Timestamp --"
//                    + DateFormat.format("dd-MM hh:mm:ss", System.currentTimeMillis()) + "-- Location Time: --"
//                    + DateFormat.format("dd-MM hh:mm:ss", currentBestLocation.getTime()) + "-- Active: --" +
//                    ACTIVE_LOCATION + "--");
//        }
        if (BaseActivity.currentActivity != null) {
            SKPosition currentPosition =
                    new SKPosition(currentBestLocation.getLatitude(), currentBestLocation.getLongitude());
            currentPosition.setHorizontalAccuracy(currentBestLocation.getAccuracy());
            currentPosition.setHeading(currentBestLocation.getBearing());
            currentPosition.setSpeed(currentBestLocation.getSpeed());
            currentPosition.setAltitude(currentBestLocation.getAltitude());
            if ((BaseActivity.currentActivity instanceof MapActivity)) {
                ((MapActivity) BaseActivity.currentActivity).updateLocation(currentPosition);
            }
        }
    }

    /**
     * Validate the concurrence between providers
     * @param location The received location fix
     * @return The current best estimate for location
     */
    private Location optimize(Location location, boolean active) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            currentBestLocationType = active;
            return location;
        } else {
            if (!active && currentBestLocationType) {
                return currentBestLocation;
            } else if (active) {
                currentBestLocationType = active;
                return location;
            } else if (location.getAccuracy() < 150) {
                return location;
            } else {
                return currentBestLocation;
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        ACTIVE_LOCATION = true;
        gotLocation(location);
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        logToFile("location manager service onStatusChanged provider= " + provider + " status= " + status);
        if (provider.equals(LocationManager.GPS_PROVIDER) && status == LocationProvider.OUT_OF_SERVICE) {
            final long WAIT_FOR_NEW_POSITION_TIME = 60 * 1000;
            gpsDisconnectedCountDownTimer = new CountDownTimer(WAIT_FOR_NEW_POSITION_TIME, WAIT_FOR_NEW_POSITION_TIME) {

                @Override
                public void onTick(long l) {

                }

                public void onFinish() {
                    switchLocationProvider();
                }
            }.start();
        }
    }

    /**
     * setter for nongps position forwarding
     */
    public void setNonGpsForwarding(boolean forward) {
        sendNonGpsPositionsToNavi = forward;
    }
}