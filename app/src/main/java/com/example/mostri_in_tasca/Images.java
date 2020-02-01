package com.example.mostri_in_tasca;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

@Entity
public class Images {

    @PrimaryKey
    private @NonNull String id;
    private @NonNull String img;

    public Images(String id, String img) {
        this.id = id;
        this.img = img;
    }

    @Ignore
    public Images(JSONObject imgJSON) {
        try{
            this.id = imgJSON.getString("id");
            this.img = imgJSON.getString("img");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    public String getImg() {
        return img;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setImg(String img) {
        this.img = img;
    }

}
