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
        catch (NullPointerException e){
            Log.d("titlebar", e.toString());
        }

        settings = getSharedPreferences("preferences",0);
        dbImages = Room.databaseBuilder(getApplicationContext(), ImagesDatabase.class,"db_images").build();

        Intent myIntent = getIntent();
        id_object = "";
        boolean tooFar = false;
        if (myIntent.hasExtra("tooFar")) {
            tooFar = myIntent.getBooleanExtra("tooFar", false);
        }
        if (myIntent.hasExtra("id")) {
            id_object = myIntent.getStringExtra("id");
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                String base64_img = dbImages.imagesDao().selectImageById(id_object).get(0).getImg();
                byte[] decodedString = Base64.decode(base64_img, Base64.DEFAULT);
                Bitmap bitmapImg = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                Drawable d = new BitmapDrawable(getResources(), bitmapImg);

                ImageView image = findViewById(R.id.imageView);
                image.setImageDrawable(d);
            }
        });

        Map map = Model.getInstance().getAllMapById(id_object);

        TextView tv = findViewById(R.id.name);
        Button btn = findViewById(R.id.fighteat);

        tv.setText(map.getName());

        tv = findViewById(R.id.type);
        if(map.getType().equals("MO")){
            tv.setText("Mostro");
            btn.setText("combatti");

            int lp = Integer.parseInt(Model.getInstance().getProfile().getLp());
            if((lp<=50 && map.getSize().equals("S")) || (lp<=75 && map.getSize().equals("M")) || (lp<=100 && map.getSize().equals("L"))){
                findViewById(R.id.death_warning).setVisibility(View.VISIBLE);
            }
        } else {
            tv.setText("Caramella");
            btn.setText("Mangia");
        }

        tv = findViewById(R.id.size);
        switch (map.getSize()){
            case "S":
                tv.setText("Piccola");
                break;
            case "M":
                tv.setText("Media");
                break;
            case "L":
                tv.setText("Grande");
                break;
        }

        if(tooFar){
            btn.setVisibility(View.GONE);
            findViewById(R.id.death_warning).setVisibility(View.GONE);
        } else {
            findViewById(R.id.distance_warning).setVisibility(View.GONE);
        }

        Button backButton = this.findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.fighteat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fighteatRequest();
            }
        });
    }

    public void fighteatRequest(){
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
                        try {
                            Map map = Model.getInstance().getAllMapById(id_object);
                            Intent intent = new Intent(getApplicationContext(), Result.class);
                            intent.putExtra("object_name", map.getName());
                            intent.putExtra("object_type", map.getType());
                            intent.putExtra("old_lp", Model.getInstance().getProfile().getLp());
                            intent.putExtra("old_xp", Model.getInstance().getProfile().getXp());
                            intent.putExtra("died", response.getBoolean("died"));
                            intent.putExtra("new_lp", response.getString("lp"));
                            intent.putExtra("new_xp", response.getString("xp"));
                            startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
