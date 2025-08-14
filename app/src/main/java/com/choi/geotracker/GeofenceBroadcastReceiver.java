package com.choi.geotracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * Geofence Broadcast Receiver class
 */

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationHelper notificationHelper = new NotificationHelper(context);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent != null && geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...");
            return;
        }

        List<Geofence> geofenceList = null;
        if (geofencingEvent != null) {
            geofenceList = geofencingEvent.getTriggeringGeofences();

            for (Geofence geofence : geofenceList) {
                Log.d(TAG, "onReceive: " + geofence.getRequestId());
            }
        }

        int transitionType = 0;
        if (geofencingEvent != null) {
            transitionType = geofencingEvent.getGeofenceTransition();
        }
        /**
         * Makes notifications based on transition type
         */
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                //Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("You are near your target location", "", MainActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                //Toast.makeText(context, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Dwelling in target area", "", MainActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                //Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Leaving the target area", "", MainActivity.class);
                break;
        }

    }
}
