package com.example.mostri_in_tasca;

import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;



public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, Style.OnStyleLoaded, PermissionsListener {
    SharedPreferences settings;
    MapDatabase db;
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

        //nascondere title bar
        try{
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        settings = getSharedPreferences("preferences",0);
        db = Room.databaseBuilder(getApplicationContext(), MapDatabase.class,"db_map").build();

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

        // Controllo che sia la prima volta
        if (checkIfFirstTime()) {
            Log.d("session_id","È la prima volta");
            // Imposto che non è più la prima volta
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("firstTime",false);
            editor.commit();
            getSessionIdRequest();
        } else {
            Log.d("session_id","Non è la prima volta");
            Log.d("session_id", "dopo la prima volta, il session_id è: "+ settings.getString("session_id", null));
            getmapRequest();
        }
    }

    public boolean checkIfFirstTime() {
        return settings.getBoolean("firstTime",true);
    }

    public void getSessionIdRequest(){
        JSONObject jsonBody = new JSONObject();
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest getSessionIdRequest = new JsonObjectRequest(
                "https://ewserver.di.unimi.it/mobicomp/mostri/register.php",
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("session_id", response.toString());
                        String session_id;
                        try {
                            session_id = response.getString("session_id");
                            Log.d("session_id", "session_id da Json response "+session_id);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString("session_id",session_id);
                            editor.commit();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("session_id", "Richiesta fallita: "+error);
                    }
                }
        );
        requestQueue.add(getSessionIdRequest);
    }

    public void getmapRequest(){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("session_id", settings.getString("session_id", null));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest getmapRequest = new JsonObjectRequest(
                "https://ewserver.di.unimi.it/mobicomp/mostri/getmap.php",
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("getmap", response.toString());

                        final List<Map> map = Model.deserialize(response);

                        //Log.d("getmap", "test: "+map.get(0).getId().toString());

                        // Metto gli oggetti della mappa scaricati nel database
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("getmap","Metto in Database");
                                db.mapDao().deleteAll(map);
                                db.mapDao().setMap(map);

                                //Una volta riempito il database, li devo spostare nel Model
                                populateMapModel();

                                //Log.d("getmap", db.mapDao().getMapName().toString() );
                                /*Model.getInstance().populate(map);
                                ArrayList<Map> test = Model.getInstance().getMapList();
                                Log.d("getmap","Model.getInstance(): " + test.get(0).getName());*/
                            }
                        });

                        /*new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                Log.d("getmap","Metto in Database");
                                db.mapDao().setMap(map);
                                //Una volta riempito il database, li devo spostare nel Model
                                //populateMapModel();
                                Model.getInstance().populate(map);
                            }
                        }.start();*/


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("getmap", "Richiesta fallita: "+error);
                    }
                }
        );
        requestQueue.add(getmapRequest);
    }

    public void populateMapModel() {
        Log.d("getmap","sono nel metodo populateMapModel");

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.d("getmap","Prendo dal Database");
                //Prendo gli studenti dal database e li metto nel Model
                List<Map> map = db.mapDao().getMap();
                Model.getInstance().populate(map);
                Log.d("getmap","getInstance()" + Model.getInstance());
            }
        });
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        Log.d("MyMap", "Map ready");
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, this);
    }

    @Override
    public void onStyleLoaded(@NonNull final Style style) {
        Log.d("MyMap", "Style ready");
        this.style = style;

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final List<Double> lats = db.mapDao().getMapLat();
                final List<Double> lons = db.mapDao().getMapLon();

                Log.d("MyMap", "lat: " + lats + " lon: "+ lons);
                for (int i = 0 ; i<3 /*lats.size()*/ ; i++){
                    Log.d("MyMap", "lat: " + lats.get((i)) + " lon: "+ lons.get((i)));

                    final int finalI = i;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            style.addImage(SYMBOL_IMAGE, BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.pika));
                            symbolManager = new SymbolManager(mapView, mapboxMap, style);
                            symbolManager.setIconAllowOverlap(true);
                            symbolManager.setTextAllowOverlap(true);
                            symbolManager.create(new SymbolOptions()
                                    .withLatLng(new LatLng(lats.get((finalI)), lons.get(finalI)))
                                    .withIconImage(SYMBOL_IMAGE)
                                    .withIconSize(0.07f));
                            symbolManager.addClickListener(new OnSymbolClickListener() {
                                @Override
                                public void onAnnotationClick(Symbol symbol) {
                                    Log.d("MyMap","Simbolo toccato");
                                }
                            });
                        }
                    });

                }
            }
        });

        /*style.addImage(SYMBOL_IMAGE, BitmapFactory.decodeResource(
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
        });*/

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