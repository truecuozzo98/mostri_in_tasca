package com.example.mostri_in_tasca;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

@Entity
public class Map {
    @PrimaryKey
    private @NonNull String id;
    @ColumnInfo(name = "lat")
    private double lat;
    @ColumnInfo(name = "lon")
    private double lon;
    @ColumnInfo(name = "type")
    private String type;
    @ColumnInfo(name = "size")
    private String size;
    @ColumnInfo(name = "name")
    private String name;


    public Map(String id, double lat, double lon, String type, String size, String name) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.type = type;
        this.size = size;
        this.name = name;
    }

    @Ignore
    public Map(JSONObject mapJSON) {
        try{
            this.id = mapJSON.getString("id");
            this.lat = mapJSON.getDouble("lat");
            this.lon = mapJSON.getDouble("lon");
            this.type = mapJSON.getString("type");
            this.size = mapJSON.getString("size");
            this.name = mapJSON.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getType() {
        return type;
    }

    public String getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLat(int lat) {
        this.lat = lat;
    }

    public void setLon(int lon) {
        this.lon = lon;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setName(String name) {
        this.name = name;
    }
}
