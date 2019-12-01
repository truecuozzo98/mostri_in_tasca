package com.example.mostri_in_tasca;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MapDao {

    @Query("SELECT * FROM map")
    List<Map> getMap();

    @Query("SELECT id FROM map")
    List<String> getMapId();

    @Query("SELECT lat FROM map")
    List<Double> getMapLat();

    @Query("SELECT lon FROM map")
    List<Double> getMapLon();

    @Query("SELECT type FROM map")
    List<String> getMapType();

    @Query("SELECT size FROM map")
    List<String> getMapSize();

    @Query("SELECT name FROM map")
    List<String> getMapName();

    @Insert
    void setMap(List<Map> map);

    @Delete
    void deleteAll(List<Map> map);

}
