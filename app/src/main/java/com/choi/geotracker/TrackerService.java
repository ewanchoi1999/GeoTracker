package com.choi.geotracker;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Service class
 */
public class TrackerService extends Service {
    private static final String TAG = "Service";
    private Location cLastLocation;

    public double totalDistance;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    PendingIntent pendingIntent;
    private boolean tracking = false;

    private final IBinder binder = new LocalBinder();
    String mDate;

    /**
     * Binding service
     */
    public class LocalBinder extends Binder {
        TrackerService getService() {
            return TrackerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;

    }

    /**
     * Creates notification  and initialises the total distance tracked to 0;
     */

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: Tracker Service");
        createNotification();
        totalDistance = 0;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int Flags, int id) {
        Log.d(TAG, "Service started");
        startTracking();
        return START_STICKY;
    }

    /**
     * Tracks location and calculates distance using fusedLocationCLient and location callback
     * and sends the data to tracking UI
     */

    public void startTracking() {
        tracking = true;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(35)
                .setMinUpdateDistanceMeters(10)
                .setMaxUpdateDelayMillis(100)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    Log.d(TAG, "No location found");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (cLastLocation != null) {
                        float[] distResults = new float[1];
                        Location.distanceBetween(cLastLocation.getLatitude(), cLastLocation.getLongitude(),
                                location.getLatitude(), location.getLongitude(), distResults);
                        totalDistance += distResults[0];
                        Log.d(TAG, "Distance: " + totalDistance);

                        @SuppressLint("DefaultLocale") String total = String.format("%.2f", totalDistance);
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction("sendToView");
                        broadcastIntent.putExtra("date", mDate);
                        broadcastIntent.putExtra("distance", total);
                        sendBroadcast(broadcastIntent);

                    }
                    cLastLocation = location;

                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
                    Looper.getMainLooper());
        }

    }

    /**
     * On destroy servvice
     */

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        removeNotification();
        tracking = false;
        Log.d("TAG", "Service destroyed");
        super.onDestroy();
    }

    /**
     * Stops location updates
     */
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Persistent notification to inform users of their location being monitored
     */
    public void createNotification() {

        String CHANNEL_NAME = "High priority channel";
        String CHANNEL_ID = "com.choi.notifications" + CHANNEL_NAME;
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription("this is the description of the channel.");
        //notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(notificationChannel);

        Intent intent = new Intent(this, MainActivity.class);

        pendingIntent = PendingIntent.getActivity
                (this, 267, intent, PendingIntent.FLAG_MUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentTitle("GeoTracker")
                .setContentText("Monitoring your location")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        startForeground(123, notification);

    }

    /**
     * Removes notification
     */
    public void removeNotification(){
        stopForeground(true);
    }

    /**
     * Sets total distance
     */
    public void setTotalDistance(){
        totalDistance = 0;

    }

    /**
     * Date setter
     * @return Date in String format
     */
    public String setDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatDate = new SimpleDateFormat("dd MMM yyyy  h:mm a");
        mDate = formatDate.format(calendar.getTime());
        return mDate;
    }


}
