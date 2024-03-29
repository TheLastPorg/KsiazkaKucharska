package com.example.kucharska.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.kucharska.model.Przepis;

import java.util.List;

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
    LiveData<Przepis> findPrzepisById(int id);

    @Query("SELECT id FROM przepisy WHERE title LIKE :title")
    LiveData<Integer> findIdByTitle(String title);

    @Query("SELECT * FROM przepisy WHERE title = :title")
    LiveData<Przepis> getPrzepisByTitle(String title);

    @Query("SELECT * FROM przepisy ORDER BY title ASC")
    LiveData<List<Przepis>> loadAllPrzepisy();
}
