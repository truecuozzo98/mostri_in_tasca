package com.example.mostri_in_tasca;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewHolder extends RecyclerView.ViewHolder {
    private TextView positionTv;
    private CircleImageView propicTv;
    private TextView xpTV;
    private TextView usernameTV;
    private TextView lpTV;
    private Activity parentActivity;

    public ViewHolder(@NonNull View itemView, Activity parentActivity) {
        super(itemView);

        this.parentActivity = parentActivity;

        positionTv = itemView.findViewById(R.id.position);
        propicTv = itemView.findViewById(R.id.profile_image);
        usernameTV = itemView.findViewById(R.id.username);
        lpTV = itemView.findViewById(R.id.lp);
        xpTV = itemView.findViewById(R.id.xp);
    }

    public void setPlayer(Player player, int i) {
        String base64_img = player.getImg();
        byte[] decodedString = Base64.decode(base64_img, Base64.DEFAULT);
        Bitmap BitmapImg = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        if(i<10){
            positionTv.setText((i+1)+" ");
        } else {
            positionTv.setText(String.valueOf(i+1));
        }


        Log.d("getRanking", "Propic: " + BitmapImg);
        Log.d("getRanking", "uid: " + player.getUsername());

        if(player.getUsername() == null || player.getUsername() == "" || player.getUsername() == "null"){
            usernameTV.setText("username non inserito");
            usernameTV.setTypeface(usernameTV.getTypeface(), Typeface.BOLD_ITALIC);
        } else {
            usernameTV.setText(player.getUsername());
        }

        if(BitmapImg == null){
            propicTv.setImageResource(R.drawable.no_propic);
        } else {
            propicTv.setImageBitmap(BitmapImg);
        }

        lpTV.setText(player.getLp());
        xpTV.setText(player.getXp());
    }
}
