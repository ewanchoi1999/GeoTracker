package com.choi.geotracker;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

/**
 * Repository Class
 */
public class WorkoutRepository {
    private TrackDataDao trackDataDao;
    private LiveData<List<TrackData>> mAllWorkouts;

    public WorkoutRepository(Application application) {
        WorkoutDatabase db = WorkoutDatabase.getDatabase(application);
        trackDataDao = db.trackDataDao();
        mAllWorkouts = trackDataDao.getAllTrackData();
    }

    public LiveData<List<TrackData>> getAllWorkouts() {
        return mAllWorkouts;
    }

    public void insert(TrackData trackData) {
        WorkoutDatabase.databaseWriteExecutor.execute(() -> {
            trackDataDao.insert(trackData);
        });
    }

    public void delete(TrackData trackData) {
        WorkoutDatabase.databaseWriteExecutor.execute(() -> {
            trackDataDao.delete(trackData);
        });
    }

    public void deleteAllWorkouts() {
        WorkoutDatabase.databaseWriteExecutor.execute(() -> {
            trackDataDao.deleteAllData();
        });
    }


}
