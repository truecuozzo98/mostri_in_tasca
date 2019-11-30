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

    public static Model getInstance() {
        return ourInstance;
    }

    private Model() {
        mapList = new ArrayList<>();
    }

    public void populate(List<Map> map) {
        mapList = new ArrayList<>(map);
    }

    public ArrayList<Map> getMapList() {
        return mapList;
    }

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
        return list;
    }
}
