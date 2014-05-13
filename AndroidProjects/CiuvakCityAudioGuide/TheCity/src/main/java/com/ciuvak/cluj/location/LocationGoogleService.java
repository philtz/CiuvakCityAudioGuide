/*
 * Copyright (c) 2013 SKOBBLER SRL.
 * Cuza Voda 1, Cluj-Napoca, Cluj, 400107, Romania
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SKOBBLER SRL 
 * ("Confidential Information"). You shall not disclose such Confidential 
 * Information and shall use it only in accordance with the terms of the license 
 * agreement you entered into with SKOBBLER SRL.
 * 
 * Created on May 28, 2013 by Filip Tudic
 * Modified on $Date$ 
 *          by $Author$
 */
package com.ciuvak.cluj.location;


import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;

import com.ciuvak.cluj.activity.BaseActivity;
import com.ciuvak.cluj.activity.MapActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.skobbler.ngx.positioner.SKPosition;

/**
 * Location service retrieves fused location from Google services
 * @author Filip Tudic
 * @version $Revision$
 */
class LocationGoogleService extends LocationProviderController implements LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    /**
     * The time interval (in millis) to receive location updates from the
     * service; leave 0 to set the fastest interval
     */
    public static final long UPDATE_INTERVAL_TIME = 1000;

    /**
     * the tag associated with this class, used for debugging
     */
    private static final String TAG = "LocationGoogleService";

    /**
     * The next two fields are used to determine when to switch to the old location
     * which at the moment is done if in a period of time of PERIOD_OF_TIME_BEFORE_SWITCHING_THE_LOCATION_SERVICE (in
     * millis)
     * the location service was disconnected NUMBER_OF_ATTEMPTS_BEFORE_SWITCHING_THE_LOCATION_SERVICE times
     */
    private static final byte NUMBER_OF_ATTEMPTS_BEFORE_SWITCHING_THE_LOCATION_SERVICE = 3;

    private static final int PERIOD_OF_TIME_BEFORE_SWITCHING_THE_LOCATION_SERVICE = 60 * 1000;

    /**
     * flag that shows that an attempt to reconnect was started
     * and is still running (waiting for the result callback);
     */
    private static boolean attemptingToReconnect;

    /**
     * records the time of the first attempt of reconnecting
     * the location service
     */
    private static long timeOfFirstAttemptToReconnect = 0;

    /**
     * counts the number of service reconnecting attempts
     */
    private static int countAttemptsToReconnect = 0;

    /**
     * Stores the best location that we received yet
     */
    private Location currentBestLocation;

    /**
     * Objects that constructs the behavior of the update location sender
     */
    private LocationRequest mLocationRequest;

    /**
     * the fused location client
     */
    private LocationClient mLocationClient;

    public LocationGoogleService(Context ctx) {
        timeOfFirstAttemptToReconnect = 0;
        countAttemptsToReconnect = 0;
        logToFile("LocationGoogleService created!");
        initLocationService(ctx);
    }

    /**
     * sets the default values of the reconnection attempts counters
     */
    private void setReconnectionsAttemptsCountersInitialValues() {
        timeOfFirstAttemptToReconnect = System.currentTimeMillis();
        countAttemptsToReconnect = 1;
    }

    /**
     * Called by Location Services if the attempt to Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        attemptingToReconnect = false;
        timeOfFirstAttemptToReconnect = 0;
        countAttemptsToReconnect = 0;
        logToFile("ON CONNECTION FAILED!!!!");
        logToFile("location google services connection failed! -- use old location approach");
        switchLocationProvider();
    }

    @Override
    public void onConnected(Bundle arg0) {
        attemptingToReconnect = false;
        logToFile("location service connected updates requested= " + periodicUpdatesRequested);
//        Logging.writeLog(TAG, "location service connected updates requested= " + periodicUpdatesRequested,
//                Logging.LOG_DEBUG);
        if (periodicUpdatesRequested) {
            startPeriodicUpdates();
        } else {
            ACTIVE_LOCATION = false;
            gotLocation(getLastLocation());
        }
    }

    /**
     * Called by Location Services if the connection to the location client
     * drops because of an error.
     */
    @Override
    public void onDisconnected() {
        logToFile("location google services disconnected");
        attemptToReconnect();
    }

    /**
     * tries to reconnect after a failed attempt to connect or
     * after a disconnection because of an error
     */
    private void attemptToReconnect() {
        if (attemptingToReconnect) {
            logToFile("location google services disconnected ignored!");
            return;
        }
        attemptingToReconnect = true;
        if (timeOfFirstAttemptToReconnect == 0) {
            setReconnectionsAttemptsCountersInitialValues();
        } else if (System.currentTimeMillis() - timeOfFirstAttemptToReconnect <=
                PERIOD_OF_TIME_BEFORE_SWITCHING_THE_LOCATION_SERVICE) {
            countAttemptsToReconnect += 1;
            if (countAttemptsToReconnect >= NUMBER_OF_ATTEMPTS_BEFORE_SWITCHING_THE_LOCATION_SERVICE) {
                switchLocationProvider();
                return;
            }
        } else {
            setReconnectionsAttemptsCountersInitialValues();
        }
        logToFile("location google services attemptToReconnect");
        new Handler().post(new Runnable() {
            public void run() {
                mLocationClient = null;
                initLocationService(BaseActivity.currentActivity);

                //also called get instance for the case when the instance needs to be recreated
                //because of the request for switching to the old localization approach
                LocationProviderController.getInstance().connectLocationService(true);
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        ACTIVE_LOCATION = true;
        gotLocation(location);
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
        currentBestLocation = location;
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
//                    debugInfo.append("REAL GOOGLE GPS POSITION:\n Lat: ").append(df.format(location.getLatitude()))
//                            .append("; ").append(" Lon: ")
//                            .append(df.format(location.getLongitude())).append("\n Time ")
//                            .append(LocationGoogleService.updatedTime).append("\n");
//                    debugInfoTV.setText(debugInfo.toString());
//                } else {
//                    debugInfoTV.setVisibility(View.GONE);
//                }
//            }
//            logToFile("RECEIVED NEW: Long --" + location.getLongitude() + "-- Lat --" + location.getLatitude() + "-- " +
//                    "Acc --"
//                    + location.getAccuracy() + "-- Provide: --" + location.getProvider() + "-- Timestamp --"
//                    + DateFormat.format("dd-MM hh:mm:ss", System.currentTimeMillis()) + "-- Location Time: --"
//                    + DateFormat.format("dd-MM hh:mm:ss", location.getTime()) + "-- Active: --" + ACTIVE_LOCATION +
//                    "--");
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
     * Create a new location client, using the enclosing class to handle
     * callbacks.
     */
    private void initLocationService(Context ctx) {
        System.out.println("Filip initLocationService");
        mLocationClient = new LocationClient(ctx, this, this);
    }

    /**
     * connects the client to the location service
     */
    public void connectLocationService(boolean initPeriodicUpdates) {
        logToFile("connectLocationService by google initPeriodicUpdates= " + initPeriodicUpdates);
        try {
            if (initPeriodicUpdates) {
                initPeriodicUpdates();
            }
            mLocationClient.connect();
        } catch (IllegalStateException ilstateex) {
            String exceptionMessage = "location google services connectLocationService IllegalStateException " +
                    "periodicUpdatesRequested= " + periodicUpdatesRequested + " ";
            logToFile(exceptionMessage);
//            Logging.writeLog(TAG, exceptionMessage + ExceptionUtils.getStackTrace(ilstateex.fillInStackTrace()),
//                    Logging.LOG_ERROR);
            switchLocationProvider();
        } catch (Exception ex) {
            //for DeadObjectException
            String exceptionMessage = "location google services connectLocationService Exception " +
                    "(DeadObjectException) periodicUpdatesRequested= " + periodicUpdatesRequested + " ";
            logToFile(exceptionMessage);
//            Logging.writeLog(TAG, exceptionMessage + ExceptionUtils.getStackTrace(ex.fillInStackTrace()),
//                    Logging.LOG_ERROR);
            switchLocationProvider();
        }
    }

    /**
     * Checks whether the location service is connected or not
     * @return the state of the location service connection
     */
    public boolean isLocationGoogleServiceConnected() {
        return mLocationClient.isConnected();
    }

    /**
     * disconnects the client from the location service and if periodic updates
     * were started, stops them
     */
    public void disconnectLocationService() {
        logToFile("disconnectLocationService by google");
        try {
            if (periodicUpdatesRequested) {
                stopPeriodicUpdates();
            }
            if (mLocationClient.isConnected()) {
                mLocationClient.disconnect();
            }
        } catch (IllegalStateException ilstateex) {
            String exceptionMessage = "location google services disconnectLocationService IllegalStateException " +
                    "periodicUpdatesRequested= " + periodicUpdatesRequested + " ";
            logToFile(exceptionMessage);
//            Logging.writeLog(TAG, exceptionMessage + ExceptionUtils.getStackTrace(ilstateex.fillInStackTrace()),
//                    Logging.LOG_ERROR);
            switchLocationProvider();
        } catch (Exception ex) {
            //for DeadObjectException
            String exceptionMessage = "location google services disconnectLocationService Exception " +
                    "(DeadObjectException) periodicUpdatesRequested= " + periodicUpdatesRequested + " ";
            logToFile(exceptionMessage);
//            Logging.writeLog(TAG, exceptionMessage + ExceptionUtils.getStackTrace(ex.fillInStackTrace()),
//                    Logging.LOG_ERROR);
            switchLocationProvider();
        }
    }

    /**
     * Constructs the LocationRequest object that sets the behavior of the new
     * location sender
     */
    private void initPeriodicUpdates() {
        setLocationRequest();

        // set the flag that updates were requested from the user
        periodicUpdatesRequested = true;
    }

    /**
     * Creates and sets the location request object
     */
    private void setLocationRequest() {
        try {
            // Create a new global location parameters object
            mLocationRequest = LocationRequest.create();

            // Set the update interval
            mLocationRequest.setInterval(UPDATE_INTERVAL_TIME);

            // Use high accuracy
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            // Set the interval ceiling to fastest
            mLocationRequest.setFastestInterval(UPDATE_INTERVAL_TIME);
        } catch (IllegalStateException ilstateex) {
            String exceptionMessage = "location google services setLocationRequest IllegalStateException " +
                    "periodicUpdatesRequested= " + periodicUpdatesRequested + " ";
            logToFile(exceptionMessage);
//            Logging.writeLog(TAG, exceptionMessage + ExceptionUtils.getStackTrace(ilstateex.fillInStackTrace()),
//                    Logging.LOG_ERROR);
            switchLocationProvider();
        } catch (Exception ex) {
            //for DeadObjectException
            String exceptionMessage = "location google services setLocationRequest Exception (DeadObjectException) " +
                    "periodicUpdatesRequested= " + periodicUpdatesRequested + " ";
            logToFile(exceptionMessage);
//            Logging.writeLog(TAG, exceptionMessage + ExceptionUtils.getStackTrace(ex.fillInStackTrace()),
//                    Logging.LOG_ERROR);
            switchLocationProvider();
        }
    }

    /**
     * Sends a request to start location updates
     */
    public void startPeriodicUpdates() {
        System.out.println("Filip startPeriodicUpdates");
        logToFile("LocationGoogleService startLocationUpdates");
        try {
            if (mLocationRequest == null) {
                setLocationRequest();
            }
            if (mLocationClient != null && mLocationClient.isConnected()) {
                System.out.println("Filip startPeriodicUpdates requestLocationUpdates");
                mLocationClient.requestLocationUpdates(mLocationRequest, this);
            }
        } catch (IllegalStateException ilstateex) {
            String exceptionMessage = "location google services startPeriodicUpdates IllegalStateException " +
                    "periodicUpdatesRequested= " + periodicUpdatesRequested + " ";
            logToFile(exceptionMessage);
//            Logging.writeLog(TAG, exceptionMessage + ExceptionUtils.getStackTrace(ilstateex.fillInStackTrace()),
//                    Logging.LOG_ERROR);
            switchLocationProvider();
        } catch (Exception ex) {
            //for DeadObjectException
            String exceptionMessage = "location google services startPeriodicUpdates Exception (DeadObjectException) " +
                    "periodicUpdatesRequested= " + periodicUpdatesRequested + " ";
            logToFile(exceptionMessage);
//            Logging.writeLog(TAG, exceptionMessage + ExceptionUtils.getStackTrace(ex.fillInStackTrace()),
//                    Logging.LOG_ERROR);
            switchLocationProvider();
        }
    }

    /**
     * Calls getLastLocation() to get the current location
     * @return the location or null if we don't have a location
     */
    public Location getLastLocation() {
        try {
            return mLocationClient.getLastLocation();
        } catch (IllegalStateException ilstateex) {
            String exceptionMessage = "location google services getLastLocation IllegalStateException " +
                    "periodicUpdatesRequested= " + periodicUpdatesRequested + " ";
            logToFile(exceptionMessage);
//            Logging.writeLog(TAG, exceptionMessage + ExceptionUtils.getStackTrace(ilstateex.fillInStackTrace()),
//                    Logging.LOG_ERROR);
            switchLocationProvider();
        } catch (Exception ex) {
            //for DeadObjectException
            String exceptionMessage = "location google services getLastLocation Exception (DeadObjectException) " +
                    "periodicUpdatesRequested= " + periodicUpdatesRequested + " ";
            logToFile(exceptionMessage);
//            Logging.writeLog(TAG, exceptionMessage + ExceptionUtils.getStackTrace(ex.fillInStackTrace()),
//                    Logging.LOG_ERROR);
            switchLocationProvider();
        }

        return null;
    }

    /**
     * Sends a request to remove location updates request them.
     */
    public void stopPeriodicUpdates() {
        logToFile("stopPeriodicUpdates by google");
        try {
            periodicUpdatesRequested = false;
            if (mLocationClient.isConnected()) {
                mLocationClient.removeLocationUpdates(this);
            }
        } catch (IllegalStateException ilstateex) {
            String exceptionMessage = "location google services stopPeriodicUpdates IllegalStateException " +
                    "periodicUpdatesRequested= " + periodicUpdatesRequested + " ";
            logToFile(exceptionMessage);
//            Logging.writeLog(TAG, exceptionMessage + ExceptionUtils.getStackTrace(ilstateex.fillInStackTrace()),
//                    Logging.LOG_ERROR);
            switchLocationProvider();
        } catch (Exception ex) {
            //for DeadObjectException
            String exceptionMessage = "location google services stopPeriodicUpdates Exception (DeadObjectException) " +
                    "periodicUpdatesRequested= " + periodicUpdatesRequested + " ";
            logToFile(exceptionMessage);
//            Logging.writeLog(TAG, exceptionMessage + ExceptionUtils.getStackTrace(ex.fillInStackTrace()),
//                    Logging.LOG_ERROR);
            switchLocationProvider();
        }
    }
}
