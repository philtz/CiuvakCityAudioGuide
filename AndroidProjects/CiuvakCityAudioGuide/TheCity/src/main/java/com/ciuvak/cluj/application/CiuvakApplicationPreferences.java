package com.ciuvak.cluj.application;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by philtz on 13/05/2014.
 */
public class CiuvakApplicationPreferences {

    /**
     * preference name
     */
    public static final String PREFS_NAME = "ciuvakCityGuidePrefs";

    /**
     * used for modifying values in a SharedPreferences prefs
     */
    private static SharedPreferences.Editor prefsEditor;

    /**
     * reference to preference
     */
    private SharedPreferences prefs;

    /**
     * the context
     */
    private Context context;

    /**
     * Default constructor. This class should only be instantiated from
     *
     * @param context - the context of the application
     */
    public CiuvakApplicationPreferences(Context context) {
        this.setContext(context);
        setDefaultPreferences();
    }

    /**
     * set the default values for preferences
     */
    private void setDefaultPreferences() {

        // initialize with their default values
        if (!getBooleanPreference(PreferenceTypes.K_PREFS_INITIALIZED)) {
            this.setPreference(PreferenceTypes.K_PREFS_INITIALIZED, PreferenceTypes.ON);
            this.setPreference(PreferenceTypes.K_FIRST_RUN, PreferenceTypes.ON);

            setDebugPreferencesDefaultValues();

            this.savePreferences();
        }
    }

    private void setDebugPreferencesDefaultValues() {
        this.setPreference(K_TEST_DATA_INPUT_MAP_ZOOM_LEVEL, 17.0f);
        this.setPreference(K_TEST_DATA_INPUT_RADIUS_UPDATE, 500);
        this.setPreference(K_TEST_DATA_INPUT_ALERT_DISTANCE, 20);
    }

    /**
     * Initializes preferences, creates the {@link android.content.SharedPreferences} and
     * {@link android.content.SharedPreferences.Editor} objects, if needed.
     */
    private void initPreferences() {
        if (prefs == null) {
            prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefsEditor = prefs.edit();
        }
    }

    // ================ PREFERENCES METHODS =========================
    // ================= editing methods =======================

    /**
     * Sets a {@link String} preference
     *
     * @param key   - the key of the preference, defined in
     * @param value - the value of the preference
     */
    public void setPreference(String key, String value) {
        initPreferences();
        prefsEditor.putString(key, value);
    }

    /**
     * Sets an int preference
     *
     * @param key   - the key of the preference, defined in
     * @param value - the value of the preference
     */
    public void setPreference(String key, int value) {
        initPreferences();
        prefsEditor.putInt(key, value);
    }

    /**
     * Sets a long preference
     *
     * @param key   - the key of the preference, defined in
     * @param value - the value of the preference
     */
    public void setPreference(String key, long value) {
        initPreferences();
        prefsEditor.putLong(key, value);
    }

    /**
     * Sets a float preference
     *
     * @param key   - the key of the preference, defined in
     * @param value - the value of the preference
     */
    public void setPreference(String key, float value) {
        initPreferences();
        prefsEditor.putFloat(key, value);
    }

    /**
     * Sets a boolean preference
     *
     * @param key   - the key of the preference, defined in
     * @param value - the value of the preference
     */
    public void setPreference(String key, boolean value) {
        initPreferences();
        prefsEditor.putBoolean(key, value);
    }

    /**
     * Commits the current changes to the preferences - to be called after
     * changing the preferences
     */
    public void savePreferences() {
        initPreferences();
        prefsEditor.commit();
    }

    /**
     * Commits the current changes to the preferences - to be called after
     * changing the preferences
     */
    public void removePreference(final String key) {
        prefsEditor.remove(key);
    }

    // ================= retrieving methods =======================

    /**
     * @return {@link String} preference for the given key or null if nothing
     * was saved
     */
    public String getStringPreference(String key) {
        initPreferences();
        try {
            return prefs.getString(key, null);
        } catch (ClassCastException ex1) {
            return String.valueOf(prefs.getInt(key, 0));
        }
    }

    /**
     * @return int preference for the given key or 0 if nothing was saved
     */
    public int getIntPreference(String key) {
        initPreferences();
        return prefs.getInt(key, 0);
    }

    /**
     * @return long preference for the given key or 0 if nothing was saved
     */
    public long getLongPreference(String key) {
        initPreferences();
        return prefs.getLong(key, 0);
    }

    /**
     * @return float preference for the given key or 0.0f if nothing was saved
     */
    public float getFloatPreference(String key) {
        initPreferences();
        return prefs.getFloat(key, 0.0f);
    }

    /**
     * @return boolean preference for the given key or false if nothing was
     * saved
     */
    public boolean getBooleanPreference(String key) {
        initPreferences();
        return prefs.getBoolean(key, false);
    }

    /**
     * Check if preferences contain a certain preference
     *
     * @param key the key of the preference whose existence will be checked
     * @return true if the preference exists, false otherwise
     */
    public boolean contains(String key) {
        return prefs.contains(key);
    }

    /**
     * @return the context
     */
    public Context getContext() {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(Context context) {
        this.context = context;
    }

    public static final String K_TEST_DATA_INPUT_MAP_ZOOM_LEVEL = "testDataInputMapZoomLevel";

    public static final String K_TEST_DATA_INPUT_RADIUS_UPDATE = "testDataInputRadiusUpdate";

    public static final String K_TEST_DATA_INPUT_ALERT_DISTANCE = "testDataInputAlertDistance";
}

