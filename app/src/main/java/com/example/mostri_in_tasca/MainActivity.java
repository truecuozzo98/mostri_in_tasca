package com.example.mostri_in_tasca;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.LongSparseArray;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.AnnotationManager;
import com.mapbox.mapboxsdk.plugins.annotation.Circle;
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager;
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions;
import com.mapbox.mapboxsdk.plugins.annotation.Line;
import com.mapbox.mapboxsdk.plugins.annotation.LineManager;
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, Style.OnStyleLoaded, PermissionsListener {
    SharedPreferences settings;
    MapDatabase dbMap;
    ImagesDatabase dbImages;
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
    private List<SymbolManager> symbolManagers = new ArrayList<>();
    private List<Line> lines = new ArrayList<>();
    private LongSparseArray<Line> lineArray;
    private List<Symbol> symbols = new ArrayList<>();
    private LongSparseArray<Symbol> symbolArray;
    private Handler handler = new Handler();
    private int delay = 10000; //Delay for 10 seconds.  One second = 1000 milliseconds.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //nasconde la title bar
        try{
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){
            Log.d("titlebar", e.toString());
        }

        settings = getSharedPreferences("preferences",0);
        dbMap = Room.databaseBuilder(getApplicationContext(), MapDatabase.class,"db_map").build();
        dbImages = Room.databaseBuilder(getApplicationContext(), ImagesDatabase.class,"db_images").build();

        permissionsManager = new PermissionsManager(this);
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);
        locationListeningCallback = new LocationListeningCallback(this);

        Mapbox.getInstance(this, "pk.eyJ1IjoidHJ1ZWN1b3p6bzk4IiwiYSI6ImNrMzRhcWF2ajBqejAzbW55MTZ5YXRlMTMifQ.IjE4RdeW6pTzUh9cyUVmxQ");
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Controllo che sia la prima volta
        if (checkIfFirstTime()) {
            Log.d("session_id","È la prima volta");
            // Imposto che non è più la prima volta
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("firstTime",false);
            editor.apply();
            getSessionIdRequest();
        } else {
            Log.d("session_id", "Non è la prima volta; il session_id è: " + settings.getString("session_id", null));
        }

        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            //mostra la posizione solo quando la mappa ha finito di caricare
            mapView.addOnDidFinishLoadingMapListener(new MapView.OnDidFinishLoadingMapListener(){
                @Override
                public void onDidFinishLoadingMap() {
                    showUserLastLocation();
                    Log.d("onCreate", "onDidFinish");
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();

        if (!PermissionsManager.areLocationPermissionsGranted(this)) {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        } else {
            getProfileRequest();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        getmapRequest();
        populateMapModel();
        populateImagesModel();

        mapView.getMapAsync(this);
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
                            editor.apply();
                            getmapRequest();
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

        Log.d("getmap", "session in dentro getmapRequest " + settings.getString("session_id", null));

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

                        final List<Map> map = Model.deserializeMap(response);

                        // Metto gli oggetti della mappa scaricati nel database
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("getmap", "Metto in Database");
                                dbMap.mapDao().deleteAll(map);
                                dbMap.mapDao().setMap(map);
                                Log.d("getimg", "size dbMap (dentro getmapRequest): " + dbMap.mapDao().getMap().size());
                                //Una volta riempito il database, li devo spostare nel Model
                                populateMapModel();

                                if(dbImages.imagesDao().getImage().size() == 0 && dbMap.mapDao().getMap().size()!=0){
                                    Log.d("getimg", "size dbMap: " + dbMap.mapDao().getMap().size());
                                    for(int i=0 ; i<dbMap.mapDao().getMap().size() ; i++){
                                        Log.d("getimg", "nel for id: " + dbMap.mapDao().getMapId().get(i));
                                        getImagesRequest(dbMap.mapDao().getMapId().get(i));
                                    }
                                    populateImagesModel();
                                }
                            }
                        });
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

    public void getImagesRequest(final String id){
        Log.d("getimg", "id: "+id);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("session_id", settings.getString("session_id", null));
            jsonBody.put("target_id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest getimgRequest = new JsonObjectRequest(
                "https://ewserver.di.unimi.it/mobicomp/mostri/getimage.php",
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("getimg", "response: " + response.toString());

                        String img = null;
                        try {
                            img = response.getString("img");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        final String finalImg = img;
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("getimg", "Metto in Database");
                                dbImages.imagesDao().addImage(new Images(id, finalImg));
                                Log.d("getimgSize", "getimg size (getImages): " + dbImages.imagesDao().getImage().size());
                            }
                        });
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("getimg", "Richiesta getimg fallita: " + error);
                    }
                }
        );
        requestQueue.add(getimgRequest);
    }

    public void populateMapModel() {
        Log.d("getmap","sono nel metodo populateMapModel");

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.d("getmap","Prendo dal Database");
                //Prendo gli studenti dal database e li metto nel Model
                List<Map> map = dbMap.mapDao().getMap();
                Model.getInstance().populateMap(map);
                Log.d("getmap","getInstance()" + Model.getInstance().getMapList());
            }
        });
    }

    public void populateImagesModel() {
        Log.d("getimg","sono nel metodo populateImagesModel");

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.d("getimg","Prendo dal Database");
                //Prendo gli studenti dal database e li metto nel Model
                List<Images> img = dbImages.imagesDao().getAllImages();
                Model.getInstance().populateImages(img);
                Log.d("getimg","getInstance img size" + Model.getInstance().getImageList().size());
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
        refreshMap();
    }

    public void refreshMap(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                getmapRequest();

                Log.d("delay", "delayed");
                Log.d("MyMap", "map size: " + Model.getInstance().getMapList().size());
                Log.d("MyMap", "img size: " + Model.getInstance().getImageList().size());
                if (PermissionsManager.areLocationPermissionsGranted(getApplicationContext())) {
                    if(location == null){
                        showUserLastLocation();
                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {

                            LineManager lineManager = new LineManager(mapView, mapboxMap, style);
                            LatLng center = new LatLng(location.getLatitude(), location.getLongitude());
                            lineManager.create(new LineOptions().withLatLngs(generateCirconference(center, 0.05, 256)));
                            lineManager.delete(lines);

                            lineArray = lineManager.getAnnotations();
                            for (int i = 0; i < lineArray.size(); i++) {
                                lines.add(lineArray.valueAt(i));
                            }

                            for(SymbolManager x : symbolManagers){
                                x.delete(symbols);
                            }
                            int size = Model.getInstance().getMapList().size();
                            for (int i = 0 ; i<size ; i++) {
                                final double lat = Model.getInstance().getMapLat(i);
                                final double lon = Model.getInstance().getMapLon(i);
                                final String id = Model.getInstance().getImageId(i);

                                String base64_img = Model.getInstance().getImageImg(i);
                                byte[] decodedString = Base64.decode(base64_img, Base64.DEFAULT);
                                final Bitmap BitmapImg = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                                style.addImage(id, BitmapImg);
                                symbolManager = new SymbolManager(mapView, mapboxMap, style);
                                symbolManager.setIconAllowOverlap(true);
                                symbolManager.setTextAllowOverlap(true);
                                Symbol symbol = symbolManager.create(new SymbolOptions()
                                        .withLatLng(new LatLng(lat, lon))
                                        .withIconImage(id)
                                        .withIconSize(0.5f)
                                        .withIconOpacity((float) 0.5));

                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                double distance = symbol.getLatLng().distanceTo(latLng);
                                if(distance<50000){
                                    Log.d("MyMap", "distance 2: " + distance);
                                    symbol.setIconOpacity((float) 1);
                                }

                                symbolManager.addClickListener(new OnSymbolClickListener() {
                                    @Override
                                    public void onAnnotationClick(Symbol symbol) {
                                        Log.d("MyMap", "Clicked on object with id: " + symbol.getIconImage());
                                        boolean flag = false;
                                        if(location!=null){
                                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                            double distance = symbol.getLatLng().distanceTo(latLng); //calcola e ritorna la distanza in metri dalla posizione dell'utente all'oggetto cliccato
                                            Log.d("MyMap", "distance: " + distance);
                                            if(distance > 50000){
                                                flag = true;
                                            }
                                            Intent intent = new Intent(getApplicationContext(), FightEat.class);
                                            intent.putExtra("id", symbol.getIconImage());
                                            intent.putExtra("tooFar", flag);
                                            startActivity(intent);
                                        }
                                    }
                                });

                                symbolManagers.add(symbolManager);
                            }

                            for(SymbolManager x : symbolManagers){
                                symbolArray = x.getAnnotations();
                                for (int j = 0; j < symbolArray.size(); j++) {
                                    symbols.add(symbolArray.valueAt(j));
                                }
                            }
                        }
                    });
                }
                // Schedule the next execution time for this runnable.
                handler.postDelayed(this, delay);
            }
        };

        // The first time this runs we don't need a delay so we immediately post.
        handler.post(runnable);
    }

    private List<LatLng> generateCirconference(LatLng centerCoordinates, double radiusInKilometers, int numberOfSides) {
        List<LatLng> positions = new ArrayList<>();
        double distanceX = radiusInKilometers / (111.319 * Math.cos(centerCoordinates.getLatitude() * Math.PI / 180));
        double distanceY = radiusInKilometers / 110.574;

        double slice = (2 * Math.PI) / numberOfSides;

        double theta;
        double x;
        double y;
        LatLng position;
        for (int i = 0; i < numberOfSides; ++i) {
            theta = i * slice;
            x = distanceX * Math.cos(theta);
            y = distanceY * Math.sin(theta);

            position = new LatLng(centerCoordinates.getLatitude() + y,
                    centerCoordinates.getLongitude() + x);
            positions.add(position);
        }
        positions.add(positions.get(0));
        return positions;
    }

    public void showUserLastLocation() {
        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_NO_POWER)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
                .build();

        locationEngine.requestLocationUpdates(request, locationListeningCallback, getMainLooper());
        locationEngine.getLastLocation(locationListeningCallback);

        LocationComponentOptions locationComponentOptions = LocationComponentOptions.builder(this).accuracyColor(0xFF0000FF).accuracyAlpha((float) 0.3).build();
        LocationComponentActivationOptions locationComponentActivationOptions = LocationComponentActivationOptions
                .builder(this, style)
                .locationComponentOptions(locationComponentOptions)
                .build();

        LocationComponent locationComponent = mapboxMap.getLocationComponent();
        locationComponent.activateLocationComponent(locationComponentActivationOptions);
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.setRenderMode(RenderMode.COMPASS);


        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                .zoom(16)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);
    }

    public void onMyPositionButtonPressed(View view) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            showUserLastLocation();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    public void profileButtonPressed(View view) {
        getProfileRequest();

        final JSONObject jsonBody = new JSONObject();
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        try {
            jsonBody.put("session_id", settings.getString("session_id", null));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest getRanking = new JsonObjectRequest(
                "https://ewserver.di.unimi.it/mobicomp/mostri/ranking.php",
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("getRanking", "primi 20 giocatori: " + response.toString());

                        List<Player> players = Model.deserializeRanking(response);
                        Model.getInstance().populatePlayers(players);
                        Log.d("getRanking", String.valueOf(Model.getInstance().getPlayersList().size()));
                        Intent intent = new Intent(getApplicationContext(), Profile.class);
                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("getRanking", "Richiesta fallita: "+error);
                    }
                }
        );
        requestQueue.add(getRanking);
    }

    public void getProfileRequest() {
        final JSONObject jsonBody = new JSONObject();
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        try {
            jsonBody.put("session_id", settings.getString("session_id", null));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest getRanking = new JsonObjectRequest(
                "https://ewserver.di.unimi.it/mobicomp/mostri/getprofile.php",
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("getprofile", "profile json (dentro getProfileRequest): " + response.toString());
                        Model.getInstance().setProfile(response);
                        Log.d("getprofile", "profile model (dentro getProfileRequest): " + Model.getInstance().getProfile().toString());

                        setProfileOnMap();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("getRanking", "Richiesta fallita: "+error);
                    }
                }
        );
        requestQueue.add(getRanking);
    }

    public void setProfileOnMap(){
        ConstraintLayout cl = findViewById(R.id.profile_map);
        if(cl != null){
            cl.setVisibility(View.VISIBLE);

            String base64_img = Model.getInstance().getProfile().getImg();
            Bitmap bitmapImg = null;
            try{
                byte[] decodedString = Base64.decode(base64_img, Base64.DEFAULT);
                bitmapImg = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ImageView iv = findViewById(R.id.propic_map);
            if(bitmapImg == null){
                iv.setImageResource(R.drawable.no_propic);
            } else {
                iv.setImageBitmap(bitmapImg);
            }

            TextView tv = findViewById(R.id.username_map);
            String uid = Model.getInstance().getProfile().getUsername();
            if(uid == null || uid.equals("") || uid.equals("null")){
                tv.setText("username non inserito");
                tv.setTypeface(tv.getTypeface(), Typeface.BOLD_ITALIC);
            } else {
                tv.setText(uid);
                tv.setTypeface(tv.getTypeface(), Typeface.NORMAL);
            }

            tv = findViewById(R.id.xp_map);
            tv.setText(String.format("Punti Esperienza: %s", Model.getInstance().getProfile().getXp()));
            tv = findViewById(R.id.lp_map);
            tv.setText(String.format("Punti Vita: %s", Model.getInstance().getProfile().getLp()));
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
            getProfileRequest();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton("Ho capito", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    permissionsManager.requestLocationPermissions(MainActivity.this);
                }
            });

            builder.setMessage("Per iniziare a giocare devi fornirci i permessi per acquisire la tua posizione");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private static class LocationListeningCallback implements LocationEngineCallback<LocationEngineResult> {

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