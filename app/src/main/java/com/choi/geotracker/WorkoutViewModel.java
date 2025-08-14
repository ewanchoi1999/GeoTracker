package com.choi.geotracker;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

/**
 * Viewmodel class that uses livedata to track changes once user stops a workout
 */
public class WorkoutViewModel extends AndroidViewModel {
    private WorkoutRepository repository;
    private final LiveData<List<TrackData>> allWorkouts;
    public WorkoutViewModel(@NonNull Application application) {
        super(application);
        repository = new WorkoutRepository(application);
        allWorkouts = repository.getAllWorkouts();
    }

    public LiveData<List<TrackData>> getAllWorkouts() {
        return allWorkouts;
    }

    public void insert(TrackData data) {
        repository.insert(data);
    }

    public void delete(TrackData data) {
        repository.delete(data);
    }

    public void deleteAll() {
        repository.deleteAllWorkouts();
    }
}
