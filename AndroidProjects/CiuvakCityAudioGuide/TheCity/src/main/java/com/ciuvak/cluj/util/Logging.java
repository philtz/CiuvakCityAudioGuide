package com.ciuvak.cluj.util;

import android.util.Log;

/**
 * Created by philtz on 12/05/2014.
 */
public class Logging {

    /**
     * key for debug logs
     */
    public static final byte LOG_DEBUG = 0;

    /**
     * key for warning logs
     */
    public static final byte LOG_WARNING = 1;

    /**
     * key for error logs
     */
    public static final byte LOG_ERROR = 2;

    /**
     * If the application is set to show logs we show logs depending on the
     * messageType
     *
     * @param tag         - the name of the class where the log is written
     * @param message     - the message log
     * @param messageType - the type of message
     *                    (LOG_DEBUG,LOG_WARNING,LOG_ERROR)
     */
    public static void writeLog(final String tag, final String message, final int messageType) {
        switch (messageType) {
            case LOG_DEBUG:
                Log.d(tag, message);
                break;
            case LOG_WARNING:
                Log.w(tag, message);
                break;
            case LOG_ERROR:
                Log.e(tag, message);
                break;
            default:
                Log.d(tag, message);
                break;
        }
    }
}

