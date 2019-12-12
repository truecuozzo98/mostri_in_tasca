package com.example.mostri_in_tasca;

import android.util.Log;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Model {
    private static final Model ourInstance = new Model();
    private ArrayList<Map> mapList;
    private ArrayList<Images> imgList;
    private ArrayList<Player> players;
    private ProfileClass profile;

    public static Model getInstance() {
        return ourInstance;
    }

    private Model() {
        mapList = new ArrayList<>();
        imgList = new ArrayList<>();
        players = new ArrayList<>();
    }

    public void populateMap(List<Map> map) {
        mapList = new ArrayList<>(map);
    }

    public void populateImages(List<Images> img) {
        imgList = new ArrayList<>(img);
    }

    public void populatePlayers(List<Player> players) {
        this.players = new ArrayList<>(players);
    }

    public void setProfile(JSONObject response) {
        String username = null;
        String xp = null;
        String lp = null;
        String img = null;

        try {
            username = String.valueOf(response.get("username"));
            xp = String.valueOf(response.get("xp"));
            lp = String.valueOf(response.get("lp"));
            img = String.valueOf(response.get("img"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.profile =  new ProfileClass(username, xp, lp, img);
    }

    public ProfileClass getProfile(){
        return profile;
    }

    public ArrayList<Map> getMapList() {
        return mapList;
    }

    public ArrayList<Player> getPlayersList() {
        return players;
    }

    public Map getAllMapById(String id){
        for(int i=0 ; i<Model.getInstance().getMapList().size() ; i++){
            if(id.equals(Model.getInstance().getMapId(i))){
                return Model.getInstance().getMapList().get(i);
            }
        }
        return null;
    }
    public String getMapId(int i){ return mapList.get(i).getId(); }
    public double getMapLat(int i){ return mapList.get(i).getLat(); }
    public double getMapLon(int i){ return mapList.get(i).getLon(); }
    public String getMapSize(int i){ return mapList.get(i).getSize(); }
    public String getMapType(int i){ return mapList.get(i).getType(); }
    public String getMapName(int i){ return mapList.get(i).getName(); }

    public ArrayList<Images> getImageList() {
        return imgList;
    }

    public String getImageId(int i){ return imgList.get(i).getId(); }
    public String getImageImg(int i){ return imgList.get(i).getImg(); }

    public static List<Map> deserializeMap(JSONObject serverResponse) {
        Log.d("getmap","Deserializzando");
        List<Map> list = new ArrayList<>();
        try {
            JSONArray getmapJSON = serverResponse.getJSONArray("mapobjects");
            Log.d("getmap", getmapJSON.toString());
            for (int i = 0; i < getmapJSON.length(); i++) {
                JSONObject mapJSON = getmapJSON.getJSONObject(i);
                Map map = new Map(mapJSON);
                list.add(map);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("getmap", "deserialize map id: " + list.get(0).getId());
        return list;
    }

    public static List<Player> deserializeRanking(JSONObject serverResponse) {
        Log.d("getRanking","Deserializzando");
        List<Player> list = new ArrayList<>();
        try {
            JSONArray getrankingJSON = serverResponse.getJSONArray("ranking");
            Log.d("getRanking", getrankingJSON.toString());
            for (int i = 0; i < getrankingJSON.length(); i++) {
                JSONObject rankingJSON = getrankingJSON.getJSONObject(i);
                Player ranking = new Player(rankingJSON);
                list.add(ranking);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

}
