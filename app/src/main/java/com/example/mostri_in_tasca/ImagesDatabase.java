package com.example.mostri_in_tasca;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Images.class}, version = 1, exportSchema = false)
public abstract class ImagesDatabase extends RoomDatabase{
    public abstract ImagesDao imagesDao();
}
