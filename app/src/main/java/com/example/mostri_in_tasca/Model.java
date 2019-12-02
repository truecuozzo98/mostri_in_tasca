package com.example.mostri_in_tasca;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Model {
    private static final Model ourInstance = new Model();
    private ArrayList<Map> mapList;
    private ArrayList<Images> imgList;

    public static Model getInstance() {
        return ourInstance;
    }

    private Model() {
        mapList = new ArrayList<>();
        imgList = new ArrayList<>();
    }

    public void populateMap(List<Map> map) {
        mapList = new ArrayList<>(map);
    }

    public void populateImages(List<Images> img) {
        imgList = new ArrayList<>(img);
    }

    public ArrayList<Map> getMapList() {
        return mapList;
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

    public static List<Map> deserialize(JSONObject serverResponse) {
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
}
