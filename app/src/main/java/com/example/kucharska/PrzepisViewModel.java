package com.example.kucharska;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

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

    LiveData<List<Przepis>> getPrzepisy() { return przepisy; }

    void insert(Przepis przepis) { repository.insert(przepis); }

    void update(Przepis przepis) { repository.update(przepis); }

    void delete(Przepis przepis) { repository.delete(przepis); }
}
