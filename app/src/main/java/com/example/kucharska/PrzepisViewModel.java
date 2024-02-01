package com.example.kucharska;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kucharska.database.PrzepisRepository;
import com.example.kucharska.model.Przepis;

import java.util.List;

public class PrzepisViewModel extends AndroidViewModel {
    private final PrzepisRepository repository;
    private final LiveData<List<Przepis>> przepisy;

    public PrzepisViewModel(@NonNull Application application) {
        super(application);
        repository = new PrzepisRepository(application);
        przepisy = repository.getPrzepisy();
        Log.d("xd", "PrzepisViewModel created " + przepisy.getValue());
    }

    LiveData<List<Przepis>> findAll() { return przepisy; }

    void insert(Przepis przepis) { repository.insert(przepis); }

    void update(Przepis przepis) { repository.update(przepis); }

    void delete(Przepis przepis) { repository.delete(przepis); }

    LiveData<Przepis> findByTitle(String title) { return repository.findByTitle(title); }
    LiveData<Integer> findIdByTitle(String title) { return repository.findIdByTitle(title); }
    LiveData<Przepis> findPrzepisById(int id) { return repository.findPrzepisById(id); }
}
