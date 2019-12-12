package com.example.mostri_in_tasca;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.KeyListener;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Profile extends AppCompatActivity {
    RankingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //nasconde la title bar
        try{
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        final TextView tv = findViewById(R.id.username);
        tv.setTag(tv.getKeyListener());
        tv.setKeyListener(null);
        ColorStateList colorStateList = ColorStateList.valueOf(Color.TRANSPARENT); //setta underline trasparente
        tv.setBackgroundTintList(colorStateList);

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
                }
            }
        });

        ImageView propic = this.findViewById(R.id.profile_image);
        propic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1 && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                ImageView propic = this.findViewById(R.id.profile_image);
                propic.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(findViewById(R.id.recyclerView)!=null){
            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new RankingAdapter(this, this, Model.getInstance().getPlayersList());
            recyclerView.setAdapter(adapter);
        }
    }

    public void onRankingButtonPressed(View v){
        Intent intent = new Intent(getApplicationContext(), Ranking.class);
        startActivity(intent);
    }


}
