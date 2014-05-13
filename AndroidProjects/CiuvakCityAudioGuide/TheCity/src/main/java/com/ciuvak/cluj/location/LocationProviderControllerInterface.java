package com.ciuvak.cluj.location;

import android.location.Location;

/**
 * An interface that needs to be implemented by
 * the location services that are used by this app
 * <p/>
 * Created by Filip Tudic on 1/6/14.
 */
interface LocationProviderControllerInterface {

    /**
     * Starts the connection to the location service
     * and optionally start the automatic periodic
     * location updates listener
     * @param initPeriodicUpdates whether to start the automatic location updates
     */
    void connectLocationService(boolean initPeriodicUpdates);

    /**
     * disconnects the current location service if connected
     */
    void disconnectLocationService();

    /**
     * Provides the latest known location by the location service
     * @return the cached location object
     */
    Location getLastLocation();

    /**
     * starts the automatic periodic updates
     * if the location service is connected
     */
    void startPeriodicUpdates();

    /**
     * stops the periodic updates if they were started
     */
    void stopPeriodicUpdates();
}
