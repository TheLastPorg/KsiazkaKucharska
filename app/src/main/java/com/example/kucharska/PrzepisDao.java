package com.example.kucharska;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import retrofit2.http.DELETE;
import retrofit2.http.GET;

@Dao
public interface PrzepisDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Przepis przepis);

    @Update
    void update(Przepis przepis);

    @Delete
    void delete(Przepis przepis);

    @Query("DELETE FROM przepisy")
    void deleteAll();

    @Query("SELECT * FROM przepisy WHERE id LIKE :id")
    Przepis findById(int id);

    @Query("SELECT * FROM przepisy WHERE title = :title")
    LiveData<Przepis> getPrzepisByTitle(String title);

    @Query("SELECT * FROM przepisy ORDER BY title ASC")
    LiveData<List<Przepis>> loadAllPrzepisy();
}
