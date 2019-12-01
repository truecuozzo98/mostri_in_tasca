package com.example.mostri_in_tasca;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ImagesDao {

    @Query("SELECT * FROM images")
    List<Images> getAllImages();

    @Query("SELECT id FROM images")
    List<String> getImageId();

    @Query("SELECT img FROM images")
    List<String> getImage();

    @Insert
    void addImage(Images img);

    @Delete
    void deleteAll(List<Images> img);
}
