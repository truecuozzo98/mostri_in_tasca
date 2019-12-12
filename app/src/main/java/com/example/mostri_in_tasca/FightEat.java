package com.example.mostri_in_tasca;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

public class FightEat extends AppCompatActivity {
    MapDatabase dbMap;
    ImagesDatabase dbImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fight_eat);

        try{
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        dbMap = Room.databaseBuilder(getApplicationContext(), MapDatabase.class,"db_map").build();
        dbImages = Room.databaseBuilder(getApplicationContext(), ImagesDatabase.class,"db_images").build();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Intent myIntent = getIntent();
                String id = "";
                boolean tooFar = false;
                if (myIntent.hasExtra("id")) {
                    id = myIntent.getStringExtra("id");
                }

                if (myIntent.hasExtra("tooFar")) {
                    tooFar = myIntent.getBooleanExtra("tooFar", false);
                }

                String base64_img = dbImages.imagesDao().selectImageById(id).get(0).getImg();
                byte[] decodedString = Base64.decode(base64_img, Base64.DEFAULT);
                Bitmap bitmapImg = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                Drawable d = new BitmapDrawable(getResources(), bitmapImg);

                ImageView image = findViewById(R.id.imageView);
                image.setImageDrawable(d);

                Map map = Model.getInstance().getAllMapById(id);
                //Log.d("fighteat", map + "   " + base64_img);

                TextView tv = findViewById(R.id.name);
                Button btn = findViewById(R.id.fighteat);

                tv.setText(map.getName());

                tv = findViewById(R.id.type);
                if(map.getType().equals("MO")){
                    tv.setText("Tipo: Mostro");
                    btn.setBackgroundColor(0xFFFF0000);
                    btn.setText("combatti");
                } else {
                    tv.setText("Tipo: Caramella");
                    btn.setBackgroundColor(0xFF00FF00);
                    btn.setText("Mangia");
                }

                tv = findViewById(R.id.size);
                if (map.getSize().equals("S")) {
                    tv.setText("Taglia: Piccola");
                } else if (map.getSize().equals("M")) {
                    tv.setText("Taglia: Media");
                } else {
                    tv.setText("Taglia: Grande");
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
    }
}
