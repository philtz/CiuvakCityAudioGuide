package com.ciuvak.cluj.location;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import android.content.Context;
import android.location.LocationManager;
import android.os.Environment;
import android.text.format.DateFormat;

import com.ciuvak.cluj.activity.BaseActivity;
import com.ciuvak.cluj.util.CiuvakUtils;

/**
 * Singleton class that manages the location services connections and connects the app to them.
 * Implements the Factory Method pattern that constructs connection to the requested location;
 * also it forces the derived classes to implement the LocationProviderControllerInterface as this
 * is the interface that will compulsory for our app and describes the current exposed functionality.
 * Created by Filip Tudic on 1/6/14.
 */
public abstract class LocationProviderController implements LocationProviderControllerInterface {

    /**
     * The default value for using the google location service
     */
    public static final boolean DEF_USE_GOOGLE_LOCATION_SERVICES = true;

    /**
     * id that matches the returned value of the getProvider() method in the
     * case of fused locations
     */
    public static final String FUSED_PROVIDER = "fused";

    /**
     * the tag associated with this class, used for debugging
     */
    private static final String TAG = "LocationProviderController";

    /**
     * Returns whether the google services are connected and is used to decide
     * whether the app can use google location service
     */
    public static Boolean locationByGoogleServices;

    /**
     * Checks whether the app has a gps receiver
     */
    public static boolean hasGPSReceiver = true;

    /**
     * the time for the latest location update
     */
    public static String updatedTime;

    /**
     * true if this was from a listener false if from last known
     * good location
     */
    protected static boolean ACTIVE_LOCATION;

    /**
     * current instance
     */
    private static LocationProviderController instance;

    /**
     * whether periodic updates were started
     */
    public boolean periodicUpdatesRequested;

    /**
     * Creates the connection to the location service and decides
     * which service will be used for the current instance of the app
     * @return the created instance
     */
    public static LocationProviderController getInstance() {
        System.out.println("Filip LocationProviderController getInstance");
        if (instance == null) {
            System.out.println("Filip LocationProviderController instance==null");
            //Logging.writeLog(TAG, "instance is null", Logging.LOG_DEBUG);
            if (locationByGoogleServices == null) {
                System.out.println("Filip LocationProviderController locationByGoogleServices==null");
                //Logging.writeLog(TAG, "locationByGoogleServices is null", Logging.LOG_DEBUG);
//                if (ForeverMapUtils.isAppInDebugMode()) {
//                    if (((ForeverMapApplication) (BaseActivity.currentActivity.getApplication()))
//                            .getApplicationPreferences()
//                            .getBooleanPreference(PreferenceTypes.DEBUG_USE_GOOGLE_LOCATION_SERVICES)) {
//                        locationByGoogleServices = ForeverMapUtils.googleServicesConnected(BaseActivity
//                                .currentActivity);
//
//                    } else {
//                        locationByGoogleServices = false;
//                    }
//                } else {
                    if (DEF_USE_GOOGLE_LOCATION_SERVICES) {
                        locationByGoogleServices = CiuvakUtils.googleServicesConnected(BaseActivity
                                .currentActivity);
                    } else {
                        locationByGoogleServices = false;
                    }
                //}
            }
            System.out.println("Filip LocationProviderController locationByGoogleServices= " + locationByGoogleServices);
            if (!locationByGoogleServices) {
                instance = new LocationManagerService(BaseActivity.currentActivity);
            } else {
                instance = new LocationGoogleService(BaseActivity.currentActivity);
            }
        }
        return instance;
    }

    /**
     * sets the default value for the instance in order to be recreated;
     * used when switching between location service providers
     */
    public static void resetLocationProviderController() {
        instance = null;
    }

    /**
     * checks if the current device has a GPS module (hardware)
     * @return true if the current device has GPS
     */
    public static boolean hasGPSReceiver() {
        final LocationManager locationManager = (LocationManager) BaseActivity.currentActivity.getSystemService
                (Context.LOCATION_SERVICE);
        for (final String provider : locationManager.getAllProviders()) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                return true;
            }
        }
        return false;
    }

    /**
     * wrapper method for forwarding non gps positions (used in old location approach)
     * @param forward
     */
    public void setNonGpsForwarding(boolean forward) {
        if (!locationByGoogleServices) {
            instance.setNonGpsForwarding(forward);
        }
    }

    /**
     * Connects to the old location approach because the current one
     * wasn't able to connect
     */
    public void switchLocationProvider() {
        logToFile("location provider controller switchLocationProvider locationByGoogleServices= " +
                LocationProviderController.locationByGoogleServices);
        LocationProviderController.locationByGoogleServices = !LocationProviderController.locationByGoogleServices;
        LocationProviderController.resetLocationProviderController();
        LocationProviderController.getInstance().connectLocationService(true);
        LocationProviderController.getInstance().startPeriodicUpdates();
    }

    /**
     * log to file
     */
    public void logToFile(String s) {
        //if (ForeverMapUtils.isAppInDebugMode()) {
            String currentTime = (String) DateFormat.format("dd-MM hh:mm:ss", System.currentTimeMillis());
            PrintWriter pw;
            try {
                pw =
                        new PrintWriter(new FileWriter(Environment.getExternalStorageDirectory() + "/positions.log",
                                true));
                pw.append(currentTime + " -- " + s + "\n");
                pw.flush();
                pw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        //}
    }
}
