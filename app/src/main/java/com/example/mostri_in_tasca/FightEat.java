package com.example.mostri_in_tasca;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.room.Room;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class FightEat extends AppCompatActivity {
    MapDatabase dbMap;
    ImagesDatabase dbImages;
    SharedPreferences settings;
    String id_object;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fight_eat);

        try{
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        settings = getSharedPreferences("preferences",0);
        dbMap = Room.databaseBuilder(getApplicationContext(), MapDatabase.class,"db_map").build();
        dbImages = Room.databaseBuilder(getApplicationContext(), ImagesDatabase.class,"db_images").build();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Intent myIntent = getIntent();
                id_object = "";
                boolean tooFar = false;
                if (myIntent.hasExtra("id")) {
                    id_object = myIntent.getStringExtra("id");
                }

                if (myIntent.hasExtra("tooFar")) {
                    tooFar = myIntent.getBooleanExtra("tooFar", false);
                }

                String base64_img = dbImages.imagesDao().selectImageById(id_object).get(0).getImg();
                byte[] decodedString = Base64.decode(base64_img, Base64.DEFAULT);
                Bitmap bitmapImg = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                Drawable d = new BitmapDrawable(getResources(), bitmapImg);

                ImageView image = findViewById(R.id.imageView);
                image.setImageDrawable(d);

                Map map = Model.getInstance().getAllMapById(id_object);
                //Log.d("fighteat", map + "   " + base64_img);

                TextView tv = findViewById(R.id.name);
                Button btn = findViewById(R.id.fighteat);

                tv.setText(map.getName());

                tv = findViewById(R.id.type);
                if(map.getType().equals("MO")){
                    tv.setText("Tipo: Mostro");
                    btn.setBackgroundColor(0xFFFF0000);
                    btn.setText("combatti");

                    final TextView deathTV = findViewById((R.id.death_warning));
                    int lp = Integer.parseInt(Model.getInstance().getProfile().getLp());
                    switch (map.getSize()){
                        case "S":
                            if(lp<=50){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        deathTV.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                            break;
                        case "M":
                            if(lp<=75){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        deathTV.setVisibility(View.VISIBLE);
                                    }
                                });                            }
                            break;
                        case "L":
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    deathTV.setVisibility(View.VISIBLE);
                                }
                            });
                            break;
                    }
                } else {
                    tv.setText("Tipo: Caramella");
                    btn.setBackgroundColor(0xFF00FF00);
                    btn.setText("Mangia");
                }

                tv = findViewById(R.id.size);
                switch (map.getSize()){
                    case "S":
                        tv.setText("Taglia: Piccola");
                        break;
                    case "M":
                        tv.setText("Taglia: Media");
                        break;
                    case "L":
                        tv.setText("Taglia: Grande");
                        break;
                }

                tv = findViewById((R.id.distance_warning));
                if(tooFar){
                    btn.setVisibility(View.GONE);
                } else {
                    tv.setVisibility(View.GONE);
                }
            }
        });

        Button backButton = this.findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });



        Button btn = findViewById(R.id.fighteat);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fighteatRequest();

            }
        });
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

    public void fighteatRequest(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);                    //crea il dialog
        builder.setTitle("Risultato");
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {          //crea il pulsate affermativo
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        final JSONObject jsonBody = new JSONObject();
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        try {
            jsonBody.put("session_id", settings.getString("session_id", null));
            jsonBody.put("target_id", id_object);
        } catch (
                JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest getRanking = new JsonObjectRequest(
                "https://ewserver.di.unimi.it/mobicomp/mostri/fighteat.php",
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("fighteat", "Richiesta riuscita: " + response.toString());
                        Map map_object = Model.getInstance().getAllMapById(id_object);
                        try {
                            if(response.getBoolean("died")){
                                builder.setMessage("GAME OVER\nSei stato sconfitto dal mostro, perdi tutti i tuoi punti esperienza!");  //setta il contenuto e il titolo del dialog
                            } else {
                                String old_lp = Model.getInstance().getProfile().getLp();
                                String old_xp = Model.getInstance().getProfile().getXp();
                                String new_lp = response.getString("lp");
                                String new_xp = response.getString("xp");
                                if(map_object.getType().equals("MO")){
                                    builder.setMessage("Congratulazioni! Hai sconfitto il mostro!\n\nPrima avevi "+old_xp+" punti esperienza e "+old_lp+" punti vita" +
                                            "\n\nOra hai "+new_xp+" punti esperienza e "+new_lp+" punti vita");
                                } else {
                                    builder.setMessage("Bene! Riguadagni punti vita!\n\nPrima avevi "+old_lp+" punti vita\n\nOra hai "+new_lp+" punti vita");
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        final AlertDialog dialog = builder.create();
                        dialog.show();                                                                   //mostra il dialog
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

}
