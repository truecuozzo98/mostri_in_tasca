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
        xpTV = itemView.findViewById(R.id.xp_map);
    }

    public void setPlayer(Player player, int i) {
        Log.d("base64", player.getImg());
        String base64_img = player.getImg();
        Bitmap bitmapImg = null;
        try{
            byte[] decodedString = Base64.decode(base64_img, Base64.DEFAULT);
            bitmapImg = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } catch (Exception e){
            Log.d("base64", e.toString());
        }

        if(i<10){
            positionTv.setText((i+1)+" ");
        } else {
            positionTv.setText(String.valueOf(i+1));
        }

        Log.d("getRanking", "Propic: " + bitmapImg);
        Log.d("getRanking", "uid: " + player.getUsername());

        if(player.getUsername() == null || player.getUsername().equals("") || player.getUsername().equals("null")){
            usernameTV.setText("username non inserito");
        } else {
            usernameTV.setText(player.getUsername());
        }

        if(usernameTV.getText()=="username non inserito"){
            usernameTV.setTypeface(usernameTV.getTypeface(), Typeface.BOLD_ITALIC);
        }

        if(bitmapImg == null){
            propicTv.setImageResource(R.drawable.no_propic);
        } else {
            propicTv.setImageBitmap(bitmapImg);
        }

        lpTV.setText(player.getLp());
        xpTV.setText(player.getXp());
    }
}
