package com.ciuvak.cluj.activity;

import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ciuvak.cluj.R;
import com.ciuvak.cluj.application.CiuvakApplication;
import com.ciuvak.cluj.location.LocationProviderController;
import com.ciuvak.cluj.util.ComputingDistance;
import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.SKMaps;
import com.skobbler.ngx.map.SKAnnotation;
import com.skobbler.ngx.map.SKMapCustomPOI;
import com.skobbler.ngx.map.SKMapPOI;
import com.skobbler.ngx.map.SKMapSettings;
import com.skobbler.ngx.map.SKMapSurfaceListener;
import com.skobbler.ngx.map.SKMapSurfaceView;
import com.skobbler.ngx.map.SKMapViewHolder;
import com.skobbler.ngx.map.SKPOICluster;
import com.skobbler.ngx.map.SKScreenPoint;
import com.skobbler.ngx.navigation.SKNavigationListener;
import com.skobbler.ngx.navigation.SKNavigationManager;
import com.skobbler.ngx.navigation.SKNavigationSettings;
import com.skobbler.ngx.navigation.SKNavigationState;
import com.skobbler.ngx.poitracker.SKDetectedPOI;
import com.skobbler.ngx.poitracker.SKPOITrackerListener;
import com.skobbler.ngx.poitracker.SKPOITrackerManager;
import com.skobbler.ngx.poitracker.SKTrackablePOI;
import com.skobbler.ngx.positioner.SKPosition;
import com.skobbler.ngx.search.SKSearchResult;

import java.util.ArrayList;
import java.util.List;

import test.ciuvak.cluj.mock.TestPoiMockData;

/**
 * Created by philtz on 24-Apr-14.
 */
public class MapActivity extends BaseActivity implements SKMapSurfaceListener {

    private static boolean mapWasCentered = false;
    MediaPlayer mediaPlayer;
    /**
     * Surface view for displaying the map
     */
    private SKMapSurfaceView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SKMapViewHolder mapViewGroup = (SKMapViewHolder) findViewById(R.id.view_group_map);
        mapView = mapViewGroup.getMapSurfaceView();
        mapView.setMapSurfaceListener(this);
        mapView.getMapSettings().setFollowerMode(SKMapSettings.MAP_FOLLOWER_MODE_NONE);
        mapView.reportNewGPSPosition(new SKPosition(23.589600, 46.769901));
        mapView.centerMapOnPositionSmooth(23.589600, 46.769901, 1000);
        mapView.setCCPIcon(SKMapSurfaceView.SKCCPArrowType.CCP_BLUE_DOT);
        applySettingsOnMapView();

        // connects the service location
        if (LocationProviderController.hasGPSReceiver) {
            LocationProviderController.getInstance().connectLocationService(true);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (LocationProviderController.hasGPSReceiver
                && !LocationProviderController.getInstance().periodicUpdatesRequested) {
            LocationProviderController.getInstance().startPeriodicUpdates();
        }

//        for (TestPoiMockData poiMockData : CiuvakApplication.getInstance().listTestPoiMockData) {
//            System.out.println(poiMockData.toString());
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        LocationProviderController.getInstance().stopPeriodicUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocationProviderController.getInstance().disconnectLocationService();
        SKMaps.getInstance().destroySKMaps();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void updateLocation(SKPosition latestPosition) {
        if (latestPosition != null) {
            mapView.reportNewGPSPosition(latestPosition);
            if (!mapWasCentered) {
                mapView.centerMapOnPositionSmooth(latestPosition.getLongitude(), latestPosition.getLatitude(), 500);
                mapWasCentered = true;
//                if (trackablePOI != null) {
//                    int distanceToPoi = (int) ComputingDistance.distanceBetween(latestPosition.getLongitude(), latestPosition.getLatitude(),
//                            trackablePOI.getLongitude(), trackablePOI.getLatitude());
//                    Toast.makeText(currentActivity, "Ciuvak updateLocation dist=" + distanceToPoi, Toast.LENGTH_SHORT).show();
//                    if (firstDistanceAlert != -1 && distanceToPoi < firstDistanceAlert) {
//                        firstDistanceAlert = -1;
//                        mediaPlayer = MediaPlayer.create(currentActivity, R.raw.fraereee);
//                        mediaPlayer.start();
//                    } else if (destinationReachAlertDistance != -1 && distanceToPoi <= destinationReachAlertDistance) {
//                        destinationReachAlertDistance = -1;
//                        mediaPlayer = MediaPlayer.create(currentActivity, R.raw.am_ajuns_ba);
//                        mediaPlayer.start();
//                        onDoubleTap(null);
//                    }
//                }
            }
        }
    }

    /**
     * Customize the map view
     */
    private void applySettingsOnMapView() {
        mapView.getMapSettings().setMapRotationEnabled(true);
        mapView.getMapSettings().setMapZoomingEnabled(true);
        mapView.getMapSettings().setMapPanningEnabled(true);
        mapView.getMapSettings().setZoomWithAnchorEnabled(true);
        mapView.getMapSettings().setInertiaRotatingEnabled(true);
        mapView.getMapSettings().setInertiaZoomingEnabled(true);
        mapView.getMapSettings().setInertiaPanningEnabled(true);
    }

    @Override
    public void onActionPan() {

    }

    @Override
    public void onActionZoom() {

    }

    @Override
    public void onSurfaceCreated() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                View chessBackground = findViewById(R.id.chess_board_background);
                chessBackground.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onScreenOrientationChanged() {

    }

    @Override
    public void onMapRegionChanged(SKCoordinate skCoordinate) {

    }

    @Override
    public void onDoubleTap(SKScreenPoint skScreenPoint) {

    }

    @Override
    public void onSingleTap(SKScreenPoint skScreenPoint) {

    }

    @Override
    public void onRotateMap() {

    }

    @Override
    public void onLongPress(SKScreenPoint skScreenPoint) {
//        SKCoordinate coord = mapView.pointToCoordinate(skScreenPoint);
//        // get the reverse geocoded address from name-browser
//        final SKSearchResult revGeocodedPlace =
//                mapView.reverseGeocodePosition(coord);
//        if (revGeocodedPlace != null) {
//            mediaPlayer = MediaPlayer.create(this, R.raw.de_inceput);
//            mediaPlayer.start();
//            createAnnotation(1, SKAnnotation.SK_ANNOTATION_TYPE_DESTINATION_FLAG, coord.getLongitude(), coord.getLatitude());
//            Toast.makeText(this, " You tapped on " + revGeocodedPlace.getName(), Toast.LENGTH_SHORT).show();
//            firstDistanceAlert = 300;
//            destinationReachAlertDistance = 50;
//            trackablePOI = new SKTrackablePOI();
//            trackablePOI.setLatitude(coord.getLatitude());
//            trackablePOI.setLongitude(coord.getLongitude());
//            // create new navi settings
//            SKNavigationSettings naviSettings = new SKNavigationSettings();
//            naviSettings.setNavigationType(SKNavigationSettings.NAVIGATION_TYPE_REAL);
//            // initialize the framework managers only one
//            navigationManager =
//                    SKNavigationManager.getInstance();
//            navigationManager.setMapView(mapView);
//            navigationManager.setNavigationListener(this);
//
////            poiTrackerManager = new SKPOITrackerManager(this);
////            poiTrackerManager.startPOITrackerWithRadius(10000, 0.5);
//
//            mapView.setCCPIcon(SKMapSurfaceView.SKCCPArrowType.CCP_AUTOSET);
//
//            navigationManager.startNavigation(naviSettings);
//        }
    }

    private void createAnnotation(int id, int type, double longitude, double latitude) {
        SKAnnotation annotation = new SKAnnotation();
        annotation.setUniqueID(id);
        annotation.setAnnotationType(type);
        annotation.setLocation(new SKCoordinate(longitude, latitude));
        mapView.addAnnotation(annotation);
    }

    @Override
    public void onInternetConnectionNeeded() {

    }

    @Override
    public void onMapActionDown(SKScreenPoint skScreenPoint) {

    }

    @Override
    public void onMapActionUp(SKScreenPoint skScreenPoint) {

    }

    @Override
    public void onAnimationsFinished() {
    }

    @Override
    public void onPOIClusterSelected(SKPOICluster skpoiCluster) {

    }

    @Override
    public void onMapPOISelected(SKMapPOI skMapPOI) {

    }

    @Override
    public void onAnnotationSelected(SKAnnotation skAnnotation) {

    }

    @Override
    public void onCustomPOISelected(SKMapCustomPOI skMapCustomPOI) {

    }

    @Override
    public void onCompassSelected() {

    }

    @Override
    public void onInternationalisationCalled(int i) {

    }
}
