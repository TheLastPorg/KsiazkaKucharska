package com.example.kucharska.database;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.kucharska.model.Przepis;

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

    public LiveData<List<Przepis>> getPrzepisy() { return przepisy; }

    public void insert(Przepis przepis) {
        PrzepisyDatabase.databaseWriteExecutor.execute(() -> przepisDao.insert(przepis));
    }

    public void update(Przepis przepis) {
        PrzepisyDatabase.databaseWriteExecutor.execute(() -> przepisDao.update(przepis));
    }

    public void delete(Przepis przepis) {
        PrzepisyDatabase.databaseWriteExecutor.execute(() -> przepisDao.delete(przepis));
    }

    public void clearDatabase() {
        PrzepisyDatabase.databaseWriteExecutor.execute(przepisDao::deleteAll);
    }
    public LiveData<Przepis> findByTitle(String title) {
        return przepisDao.getPrzepisByTitle(title);
    }

    public LiveData<Integer> findIdByTitle(String title) {
        return przepisDao.findIdByTitle(title);
    }

    public LiveData<Przepis> findPrzepisById(int id) {
        return przepisDao.findPrzepisById(id);
    }
}
