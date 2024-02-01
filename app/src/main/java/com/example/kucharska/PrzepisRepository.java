package com.example.kucharska;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;

public class PrzepisRepository {
    private final PrzepisDao przepisDao;
    private PrzepisyDatabase database;
    private final LiveData<List<Przepis>> przepisy;

    public PrzepisRepository(Application application) {
        database = PrzepisyDatabase.getDatabase(application);
        this.przepisDao = database.przepisDao();
        this.przepisy = przepisDao.loadAllPrzepisy();
        Log.d("xd", "PrzepisRepository created " + przepisy.getValue());
    }

    LiveData<List<Przepis>> getPrzepisy() { return przepisy; }

    void insert(Przepis przepis) {
        PrzepisyDatabase.databaseWriteExecutor.execute(() -> przepisDao.insert(przepis));
    }

    void update(Przepis przepis) {
        PrzepisyDatabase.databaseWriteExecutor.execute(() -> przepisDao.update(przepis));
    }

    void delete(Przepis przepis) {
        PrzepisyDatabase.databaseWriteExecutor.execute(() -> przepisDao.delete(przepis));
    }

    public void clearDatabase() {
        PrzepisyDatabase.databaseWriteExecutor.execute(przepisDao::deleteAll);
    }
}
