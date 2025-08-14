package com.choi.geotracker;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity class for room database
 */
@Entity(tableName = "workout_data")
public class TrackData {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String distance;
    public String timeTaken;
    public String avgPace;
    public String workoutType;
    public String date;

    /**
     * Constructor for a workout
     * @param distance
     * @param timeTaken
     * @param avgPace
     * @param workoutType
     * @param date
     */
    public TrackData(String distance, String timeTaken, String avgPace, String workoutType, String date) {
        this.distance = distance;
        this.timeTaken = timeTaken;
        this.avgPace = avgPace;
        this.workoutType = workoutType;
        this.date = date;
    }
    // Getters, and setters

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getDistance() {
        return distance;
    }

    public String getTimeTaken() {
        return timeTaken;
    }

    public String getAvgPace() {
        return avgPace;
    }

    public String getWorkoutType() {
        return workoutType;
    }

    public String getDate() {
        return date;
    }
}
