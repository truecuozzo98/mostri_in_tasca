package com.example.mostri_in_tasca;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

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

import java.util.List;

public class Ranking extends AppCompatActivity {
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        //nasconde la title bar
        try{
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        settings = getSharedPreferences("preferences",0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final RankingAdapter adapter = new RankingAdapter(this, this, Model.getInstance().getPlayersList());
        recyclerView.setAdapter(adapter);

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

                        final List<Player> players = Model.deserializeRanking(response);
                        Log.d("getRanking", "Size players: " + players.size());
                        Log.d("getRanking", "primo giocatore: " + players.get(0).getUsername());

                        Model.getInstance().populatePlayers(players);

                        for(int i=0 ; i<20 ; i++) {
                            Log.d("getRanking", "i: " +(i+1) + " uid: " + Model.getInstance().getPlayersList().get(i).getUsername() +
                                    " IMG: " + Model.getInstance().getPlayersList().get(i).getImg());
                        }

                        adapter.notifyDataSetChanged();
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


