package com.choi.geotracker;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Database class that extends room database
 */
@Database(entities = {TrackData.class}, version = 4, exportSchema = false)
public abstract class WorkoutDatabase extends RoomDatabase {

    public abstract TrackDataDao trackDataDao();

    private static volatile WorkoutDatabase INSTANCE;

    static final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();

    public static WorkoutDatabase getDatabase(final Context context)  {
        if(INSTANCE == null) {
            synchronized (WorkoutDatabase.class) {
                if(INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), WorkoutDatabase.class, "workout_database" ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}
