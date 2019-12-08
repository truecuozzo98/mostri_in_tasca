package com.example.mostri_in_tasca;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Player extends ArrayList<Player> {
    private String username;
    private String img;
    private String xp;
    private String lp;

    public Player(String username, String img, String xp, String lp) {
        this.username = username;
        this.img = img;
        this.xp = xp;
        this.lp = lp;
    }

    public Player(JSONObject mapJSON) {
        try{
            this.username = mapJSON.getString("username");
            this.img = mapJSON.getString("img");
            this.xp = mapJSON.getString("xp");
            this.lp = mapJSON.getString("lp");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getXp() {
        return xp;
    }

    public void setXp(String xp) {
        this.xp = xp;
    }

    public String getLp() {
        return lp;
    }

    public void setLp(String lp) {
        this.lp = lp;
    }
}
