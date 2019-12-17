package com.example.mostri_in_tasca;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.KeyListener;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Profile extends AppCompatActivity {
    RankingAdapter adapter;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //nasconde la title bar
        try{
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){
            Log.d("titlebar", e.toString());
        }

        settings = getSharedPreferences("preferences",0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //getProfileRequest();

        final TextView tv = findViewById(R.id.username);
        Log.d("usernameProfile", Model.getInstance().getProfile().getUsername());
        if(Model.getInstance().getProfile().getUsername() == null || Model.getInstance().getProfile().getUsername().equals("null")){
            tv.setText("");
            tv.setHint("username non inserito");
        } else {
            tv.setText(Model.getInstance().getProfile().getUsername());
        }
        tv.setTag(tv.getKeyListener());
        tv.setKeyListener(null);                                                    //rende editText non modificabile
        tv.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));        //setta underline dell'editText trasparente

        final Button mod = this.findViewById(R.id.mod);
        mod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tv.getKeyListener()==null){
                    tv.setKeyListener((KeyListener) tv.getTag());
                    ColorStateList colorStateList = ColorStateList.valueOf(Color.RED);
                    tv.setBackgroundTintList(colorStateList); //set underline rosso

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); //apre la tastiera
                    if (imm != null) {
                        imm.showSoftInput(tv, InputMethodManager.SHOW_IMPLICIT);
                    }
                    mod.setText("Invia");
                } else {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); //chiude la tastiera
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(tv.getWindowToken(), 0);
                    }
                    tv.setKeyListener(null);
                    ColorStateList colorStateList = ColorStateList.valueOf(Color.TRANSPARENT);
                    tv.setBackgroundTintList(colorStateList);
                    mod.setText("Modifica");
                    setProfileRequest(tv.getText().toString(), Model.getInstance().getProfile().getImg());
                }
            }
        });

        TextView xpTv = findViewById(R.id.xp_map);
        xpTv.setText("Punti esperienza: " + Model.getInstance().getProfile().getXp());
        TextView lpTv = findViewById(R.id.lp);
        lpTv.setText("Punti vita: " + Model.getInstance().getProfile().getLp());

        //crea un listener che al tocco sull'immagine apre la galleria
        ImageView propic = this.findViewById(R.id.profile_image);
        String base64_img = Model.getInstance().getProfile().getImg();
        Log.d("propic", "Model.getInstance().getProfile().getImg(): " + Model.getInstance().getProfile().getImg());
        if(base64_img != null && !base64_img.equals("") && !base64_img.equals("null")){
            byte[] decodedString = Base64.decode(base64_img, Base64.DEFAULT);
            Bitmap BitmapImg = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            propic.setImageBitmap(BitmapImg);
        }

        propic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), 1);
            }
        });

        if(findViewById(R.id.recyclerView)!=null){
            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new RankingAdapter(this, this, Model.getInstance().getPlayersList());
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    //prende immagine dalla galleria e la carica
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1 && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();
                String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                Log.d("propic", "onActivityResult: " + encoded);

                setProfileRequest(Model.getInstance().getProfile().getUsername(), encoded);

                ImageView propic = this.findViewById(R.id.profile_image);
                propic.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onRankingButtonPressed(View v){
        Intent intent = new Intent(getApplicationContext(), Ranking.class);
        startActivity(intent);
    }

    public void setProfileRequest(String username, final String img) {
        Log.d("propic", "setProfileRequest: " + img);

        Model.getInstance().getProfile().setUsername(username);
        Model.getInstance().getProfile().setImg(img);

        final JSONObject jsonBody = new JSONObject();
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        try {
            jsonBody.put("session_id", settings.getString("session_id", null));
            jsonBody.put("username", username);
            jsonBody.put("img", img);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest getRanking = new JsonObjectRequest(
                "https://ewserver.di.unimi.it/mobicomp/mostri/setprofile.php",
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("setProfile", "Richiesta riuscita: "+response);
                        Log.d("setProfile", "model: " + Model.getInstance().getProfile().getImg());
                        //getProfileRequest();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("setProfile", "Richiesta fallita: "+error);
                    }
                }
        );
        requestQueue.add(getRanking);
    }
}
