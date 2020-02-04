package com.example.mostri_in_tasca;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Result extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try{
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){
            Log.d("titlebar", e.toString());
        }

        setContentView(R.layout.activity_result);
        ImageView imageIv = findViewById(R.id.image);
        TextView resultTv = findViewById(R.id.result);
        TextView beforeTv = findViewById(R.id.old);
        Button btn = findViewById(R.id.back);

        String object_name = null;
        String object_type = null;
        String old_lp = null;
        String old_xp = null;
        String new_lp = null;
        String new_xp = null;
        boolean died = false;

        if(getIntent().getExtras() != null){
            object_name = getIntent().getExtras().getString("object_name");
            object_type = getIntent().getExtras().getString("object_type");
            old_lp = getIntent().getExtras().getString("old_lp");
            old_xp = getIntent().getExtras().getString("old_xp");
            died = getIntent().getExtras().getBoolean("died");
            new_lp = getIntent().getExtras().getString("new_lp");
            new_xp = getIntent().getExtras().getString("new_xp");
        }

        if(died){
            imageIv.setImageResource(R.drawable.death_skull);
            resultTv.setText(String.format("Oh no! Sei stato sconfitto da %s", object_name));
            beforeTv.setText("Riparti con 100 punti vita e 0 punti esperienza");
            btn.setText("Ricomincia");
        } else {
            if("MO".equals(object_type)){
                imageIv.setImageResource(R.drawable.crossed_swords);
                resultTv.setText(String.format("Grande! Hai sconfitto %s", object_name));
                beforeTv.setText(String.format("Prima avevi %s punti vita e %s punti esperienza. Ora hai %s punti vita e %s punti esperienza", old_lp, old_xp, new_lp, new_xp));
            } else {
                imageIv.setImageResource(R.drawable.heart);
                resultTv.setText(String.format("Bene! Hai mangiato un %s", object_name));
                beforeTv.setText(String.format("Prima avevi %s punti vita. Ora hai %s punti vita ", old_lp, new_lp));
            }
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
