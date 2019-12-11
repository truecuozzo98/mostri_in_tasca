package com.example.mostri_in_tasca;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
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
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
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
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 15*1000; //Delay for 15 seconds.  One second = 1000 milliseconds.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //nasconde la title bar
        try{
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

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
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();

        if (!PermissionsManager.areLocationPermissionsGranted(this)) {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        /*handler.postDelayed(runnable = new Runnable() {
                public void run() {
                    //do something

                    handler.postDelayed(runnable, 100);
                }
            }, 100);*/

        /*handler.postDelayed(runnable = new Runnable() {
            public void run() {
                //do something

                handler.postDelayed(runnable, delay);
            }
        }, delay);*/

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

        Log.d("MyMap", "map size" + Model.getInstance().getMapList().size());
        Log.d("MyMap", "img size" + Model.getInstance().getImageList().size());
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            int size = Model.getInstance().getMapList().size();
            for (int i = 0 ; i<size ; i++) {
                final double lat = Model.getInstance().getMapLat(i);
                final double lon = Model.getInstance().getMapLon(i);
                final String id = Model.getInstance().getImageId(i);

                Log.d("MyMap", "finalId: " + id);

                String base64_img = Model.getInstance().getImageImg(i);
                byte[] decodedString = Base64.decode(base64_img, Base64.DEFAULT);
                final Bitmap BitmapImg = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                //BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.cbimage)

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //Log.d("MyMap", "Prima di remove immagine è presente?: " + style.getImage(id));
                        //style.removeImage(id);
                        //Log.d("MyMap", "Prima di add immagine è presente?: " + style.getImage(id));
                        style.addImage(id, BitmapImg);
                        //Log.d("MyMap", "Dopo immagine è presente?: " + style.getImage(id));
                        symbolManager = new SymbolManager(mapView, mapboxMap, style);
                        symbolManager.setIconAllowOverlap(true);
                        symbolManager.setTextAllowOverlap(true);
                        symbolManager.create(new SymbolOptions()
                                .withLatLng(new LatLng(lat, lon))
                                .withIconImage(id)
                                .withIconSize(0.5f));

                        symbolManager.addClickListener(new OnSymbolClickListener() {
                            @Override
                            public void onAnnotationClick(Symbol symbol) {
                                /*Log.d("MyMap", "Bitmap: " + BitmapImg);
                                 Log.d("MyMap", "id: " + id);
                                 Log.d("MyMap", "Clicked on object with id: " + symbol.getIconImage());*/
                                Log.d("MyMap", "Clicked on object with id: " + symbol.getIconImage());
                                Intent intent = new Intent(getApplicationContext(), FightEat.class);
                                intent.putExtra("id", symbol.getIconImage());
                                startActivity(intent);
                            }
                        });
                    }
                });
            }
            /*if(symbolManager.getAnnotations() != null) {
                for (int j = 0; j < symbolManager.getAnnotations().size(); j++) {
                    Symbol symbol = symbolManager.getAnnotations().get(i);

                    if(symbol != null)
                    if (symbol.getLatLng().distanceTo(locationLatLng) > 250) {
                        Log.e("mysymid", symbol.getId() + "");

                        symbolManager.getAnnotations().remove(symbol.getId());
                        symbolManager.delete(symbol);
                        symbolOptionsList.remove(j);
                        symbolManager.updateSource();
                        //moneyMap.remove(money.getG());
                    }
                }
             }*/
        }
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
                .zoom(10)
                //.zoom(15)
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
        Intent intent = new Intent(getApplicationContext(), Profile.class);
        startActivity(intent);
    }

    public void rankingButtonPressed(View view) {
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
                        Intent intent = new Intent(getApplicationContext(), Ranking.class);
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
            //mapView.invalidate();
            mapView.getMapAsync(this);
        } else {
            Toast.makeText(this, "Permesso non dato", Toast.LENGTH_LONG).show();
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