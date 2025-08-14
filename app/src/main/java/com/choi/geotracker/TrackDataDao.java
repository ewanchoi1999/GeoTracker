package com.choi.geotracker;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Dao class for database
 */
@Dao
public interface TrackDataDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(TrackData data);

    @Delete
    void delete(TrackData data);
    @Query("DELETE FROM workout_data")
    void deleteAllData();

    @Query("SELECT * FROM workout_data ORDER BY date DESC")
    LiveData<List<TrackData>> getAllTrackData();
}
