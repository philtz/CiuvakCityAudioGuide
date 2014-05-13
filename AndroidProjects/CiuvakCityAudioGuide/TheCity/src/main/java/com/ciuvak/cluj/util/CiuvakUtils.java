package com.ciuvak.cluj.util;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by philtz on 29/04/2014.
 */
public class CiuvakUtils {

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    public static boolean googleServicesConnected(Context ctx) {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(ctx);
        //Logging.writeLog(TAG, "googleServicesConnected resultCode= " + resultCode, Logging.LOG_DEBUG);

        // If Google Play services is available
        return ConnectionResult.SUCCESS == resultCode;
    }
}
