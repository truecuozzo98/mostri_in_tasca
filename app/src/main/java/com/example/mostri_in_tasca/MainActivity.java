package com.example.mostri_in_tasca;

import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.lang.ref.WeakReference;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, Style.OnStyleLoaded, PermissionsListener {

    private MapView mapView;
    private MapboxMap mapboxMap = null;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationListeningCallback locationListeningCallback;
    private Style style;
    private Location location;
    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private SymbolManager symbolManager;
    public static final String SYMBOL_IMAGE = "pika";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        Mapbox.getInstance(this, "pk.eyJ1IjoidHJ1ZWN1b3p6bzk4IiwiYSI6ImNrMzRhcWF2ajBqejAzbW55MTZ5YXRlMTMifQ.IjE4RdeW6pTzUh9cyUVmxQ");
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        permissionsManager = new PermissionsManager(this);
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);
        locationListeningCallback = new LocationListeningCallback(this);


    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(locationListeningCallback);
        }
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        Log.d("MyMap", "Map ready");
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, this);
    }

    @Override
    public void onStyleLoaded(@NonNull Style style) {
        Log.d("MyMap", "Style ready");
        this.style = style;

        style.addImage(SYMBOL_IMAGE, BitmapFactory.decodeResource(
                MainActivity.this.getResources(), R.drawable.pika));
        symbolManager = new SymbolManager(mapView, mapboxMap, style);
        symbolManager.setIconAllowOverlap(true);
        symbolManager.setTextAllowOverlap(true);
        symbolManager.create(new SymbolOptions()
                .withLatLng(new LatLng(45.4767, 9.2319))
                .withIconImage(SYMBOL_IMAGE)
                .withIconSize(0.07f));
        symbolManager.addClickListener(new OnSymbolClickListener() {
            @Override
            public void onAnnotationClick(Symbol symbol) {
                Log.d("MyMap","Simbolo toccato");
            }
        });

        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(51.50550, -0.07520))
                .zoom(10)
                .tilt(20)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);

        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            showUserLastLocation();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    public void showUserLastLocation() {
        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_NO_POWER)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
                .build();

        locationEngine.requestLocationUpdates(request, locationListeningCallback, getMainLooper());
        locationEngine.getLastLocation(locationListeningCallback);

        LocationComponent locationComponent = mapboxMap.getLocationComponent();
        locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(this, style).build());
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.setRenderMode(RenderMode.COMPASS);
    }

    public void onCenterButtonPressed(View view) {
        if (location != null) {
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                    .zoom(10)
                    .tilt(20)
                    .build();
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);
        }

    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            showUserLastLocation();
        } else {
            Toast.makeText(this, "Permesso non dato", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private static class LocationListeningCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<MainActivity> activityWeakReference;
        private MainActivity mainActivity;

        LocationListeningCallback(MainActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
            mainActivity = activity;
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            mainActivity.location = result.getLastLocation();
        }

        @Override
        public void onFailure(@NonNull Exception exception) {

        }
    }

}