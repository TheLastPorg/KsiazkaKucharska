package com.example.kucharska.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.kucharska.model.Przepis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Przepis.class}, version = 1, exportSchema = false)
public abstract class PrzepisyDatabase extends RoomDatabase {
    private static PrzepisyDatabase INSTANCE;
    static final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();

    public abstract PrzepisDao przepisDao();

    public static synchronized PrzepisyDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            PrzepisyDatabase.class, "przepisy_database")
                    .build();
        }
        return INSTANCE;
    }

}
