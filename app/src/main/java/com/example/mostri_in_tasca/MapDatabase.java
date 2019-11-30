package com.example.mostri_in_tasca;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Map.class}, version = 1, exportSchema = false)
public abstract class MapDatabase extends RoomDatabase {
    public abstract MapDao mapDao();
}
