package com.example.mostri_in_tasca;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class Ranking extends AppCompatActivity {
    SharedPreferences settings;
    RankingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

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
    protected void onResume() {
        super.onResume();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RankingAdapter(this, this, Model.getInstance().getPlayersList());
        recyclerView.setAdapter(adapter);
    }

}


