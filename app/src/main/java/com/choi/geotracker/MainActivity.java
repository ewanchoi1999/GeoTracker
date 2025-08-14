package com.choi.geotracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMapLongClickListener {

    /**
     Initialising of variables
     */
    long currentTime = 0;
    Intent trackerServiceIntent;

    WorkoutDatabase workoutDatabase;
    private static final String TAG = "Maps";

    private GoogleMap myMap;
    private GeofencingClient geofencingClient;
    private GeofenceUtils geofenceHelper;

    public TrackerService trackerService;
    private boolean isBound = false;
    TextView mpace;
    TextView mdistance;

    Chronometer chronometer;
    private boolean isWorkout = false;
    private boolean isTimer = false;
    private float GEOFENCE_RADIUS = 200;
    private String GEOFENCE_ID = "SOME_GEOFENCE_ID";
    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    long timeWhenStopped = 0;
    double paceVal = 0;
    public String formattedTime;

    BroadcastReceiver mReceiver;
    Button newWorkout, pause;
    String selectedWorkoutType;

    String[] workoutType = {"Walk", "Jog", "Run", "Cycle"};

    /**
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     *
     * Sets view on create, shows a tip on how to set geofence, starts service and registers the broadcast receiver to get distance and location updates
     * from the service. Then draws the map on the screen and clears all geofences.
     */
    @SuppressLint({"DefaultLocale", "NonConstantResourceId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        View root = findViewById(R.id.root);
        Snackbar.make(root, "Long hold on the map to set a target location to receive notifications", Snackbar.LENGTH_SHORT).show();
        trackerServiceIntent = new Intent(this, TrackerService.class);
        startForegroundService(trackerServiceIntent);
        /**
         * Register broadcast receiver
         */
        mReceiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter("sendToView");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceUtils(this);
        removeGeofence();
        LayoutInflater inflater = LayoutInflater.from(this);

        /**
         * Finding views and buttons by id
         */
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        LinearLayout l1 = (LinearLayout) findViewById(R.id.container1);
        LinearLayout l2 = (LinearLayout) findViewById(R.id.container2);
        LinearLayout l3 = (LinearLayout) findViewById(R.id.container3);
        chronometer = findViewById(R.id.timer);
        mdistance = findViewById(R.id.distance);
        mpace = findViewById(R.id.pace);
        newWorkout = findViewById(R.id.new_workout);
        pause = findViewById(R.id.pause_btn);
        newWorkout.setBackgroundColor(getResources().getColor(R.color.purple_500));
        /**
         * Switches between the workouts and home view
         */
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                return true;
            } else if (itemId == R.id.workouts) {
                Intent workout = new Intent(MainActivity.this, WorkoutView.class);
                startActivity(workout);

                return true;
            }
            return false;
        });
        /**
         * Start workout button. Allows user to select a type of workout.
         */
        newWorkout.setOnClickListener(view -> {
            if (!isWorkout) {
                trackerService.setTotalDistance();
                mdistance.setText("Distance: 0.0m");
                View customView = inflater.inflate(R.layout.custom_alert_dialog, null);
                Spinner customSpinner = customView.findViewById(R.id.custom_spinner);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, workoutType);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                customSpinner.setAdapter(adapter);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("What type of workout would you like to track?");
                builder.setView(customView);

                /**
                 * Shows controls for workout once workout starts
                 */
                builder.setPositiveButton("Start Workout", (alertDialog, which) -> {
                    selectedWorkoutType = customSpinner.getSelectedItem().toString();
                    Toast.makeText(MainActivity.this, "Selected Workout Type: " + selectedWorkoutType, Toast.LENGTH_SHORT).show();
                    l1.setVisibility(View.VISIBLE);
                    l2.setVisibility(View.VISIBLE);
                    l3.setVisibility(View.VISIBLE);
                    pause.setVisibility(View.VISIBLE);
                    chronometer.setVisibility(View.VISIBLE);
                    mpace.setVisibility(View.VISIBLE);
                    mdistance.setVisibility(View.VISIBLE);
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                    isWorkout = true;
                    newWorkout.setText("Stop Workout");
                    newWorkout.setBackgroundColor(getResources().getColor(R.color.red));
                });

                builder.setNegativeButton("Cancel", (alertDialog, which) -> {
                   alertDialog.cancel();
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                /**
                 * Stops workout and hides UI controls, and passes the workout values to the workoutview
                 */
                Toast.makeText(MainActivity.this, "Workout Stopped", Toast.LENGTH_SHORT).show();
                l1.setVisibility(View.GONE);
                l2.setVisibility(View.GONE);
                l3.setVisibility(View.GONE);
                pause.setVisibility(View.GONE);
                chronometer.setVisibility(View.GONE);
                mpace.setVisibility(View.GONE);
                newWorkout.setText("New Workout");
                newWorkout.setBackgroundColor(getResources().getColor(R.color.purple_500));
                mdistance.setVisibility(View.GONE);
                String date = trackerService.setDate();
                isWorkout = false;
                chronometer.stop();
                saveWorkout();
                chronometer.setBase(SystemClock.elapsedRealtime());
                //save workout

            }
        });
        /**
         * Pauses the current timer
         */
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTimer) { //if timer not paused
                    currentTime = SystemClock.elapsedRealtime() + timeWhenStopped;
                    chronometer.setBase(currentTime);
                    chronometer.start();
                    pause.setText("Pause Workout");
                    pause.setBackgroundColor(getResources().getColor(R.color.purple_500));
                } else {
                    chronometer.stop();
                    timeWhenStopped = chronometer.getBase() - SystemClock.elapsedRealtime();
                    pause.setText("Resume Workout");
                    pause.setBackgroundColor(getResources().getColor(R.color.green));
                }
                isTimer = !isTimer;
            }
        });

    }

    /**
     * Stops workout
     */
    private void saveWorkout() {
        String workoutType = selectedWorkoutType;
        String workoutDistance = mdistance.getText().toString();
        String workoutPace = mpace.getText().toString();
        String workoutDate = trackerService.setDate();
        String workoutTime = chronometer.getText().toString();

        Intent saved = new Intent(MainActivity.this, WorkoutView.class);
        saved.putExtra("time", workoutTime);
        saved.putExtra("date", workoutDate);
        saved.putExtra("distance", workoutDistance);
        saved.putExtra("pace", workoutPace);
        saved.putExtra("type", workoutType);
        startActivity(saved);
    }

    /**
     * Format time
     * @return Time in seconds to calculate pace
     */
    private long getFormattedTime() {
        if(!isTimer) {
            long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase(); //time
            return TimeUnit.MILLISECONDS.toSeconds(elapsedMillis);
        }
        return 0;
    }

    /**
     * Service binder
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            TrackerService.LocalBinder binder = (TrackerService.LocalBinder) iBinder;
            trackerService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };

    /**
     * Binds and unbinds service
     */
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, TrackerService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        LatLng notts = new LatLng(52.95326304831025, -1.187324760221763);
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(notts, 15));
        enableUserLocation();
        myMap.setOnMapLongClickListener(this);

    }


    @Override
    public boolean onMyLocationButtonClick() {
        // Handle location button click event
        return false;
    }

    /**
     * Checks if permissions have been granted. If not, shows the permission granting screen
     */
    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            myMap.setMyLocationEnabled(true);
        } else {
            //Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                //We need to show user a dialog for displaying why the permission is needed and then ask for the permission...
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
        }
    }

    /**
     * Sets mylocation enabled to true once permissions granted
     * @param requestCode The request code passed in {@link #requestPermissions(
     * android.app.Activity, String[], int)}
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                myMap.setMyLocationEnabled(true);
            } else {
                Toast.makeText(this, "No location access", Toast.LENGTH_SHORT).show();

            }
        }

        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                Toast.makeText(this, "You can add geofences...", Toast.LENGTH_SHORT).show();
            } else {
                //We do not have the permission..
                Toast.makeText(this, "Background location access is neccessary for geofences to trigger...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Creates a geofence that provides location based reminders when the user is near by
     * @param latLng
     */
    @Override
    public void onMapLongClick(LatLng latLng) {
        //We need background permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            handleMapLongClick(latLng);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                //We show a dialog and ask for permission
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
            }
        }

    }

    /**
     * clears map and adds geofence
     * @param latLng
     */
    private void handleMapLongClick(LatLng latLng) {
        myMap.clear();
        addMarker(latLng);
        addCircle(latLng, GEOFENCE_RADIUS);
        addGeofence(latLng, GEOFENCE_RADIUS);
    }

    private void addGeofence(LatLng latLng, float radius) {

        Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: Geofence Added..."))
                    .addOnFailureListener(e -> {
                        String errorMessage = geofenceHelper.getErrorString(e);
                        Log.d(TAG, "onFailure: " + errorMessage);
                    });
        }
    }

    /**
     * removes geofences
     */
    private void removeGeofence() {
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            geofencingClient.removeGeofences(pendingIntent)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: GeofenceRemoved..."))
                    .addOnFailureListener(e -> {
                        String errorMessage = geofenceHelper.getErrorString(e);
                        Log.d(TAG, "onFailure: " + errorMessage);
                    });
        }
    }

    /**
     * marker and circle used to denote geofence
     * @param latLng
     */
    private void addMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        myMap.addMarker(markerOptions);
    }

    private void addCircle(LatLng latLng, float radius) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        myMap.addCircle(circleOptions);
    }

    /**
     * Broadcast receiver to constantly receive updates from service and updates the ui
     */
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String date = intent.getStringExtra("date");
            String dist = intent.getStringExtra("distance");
            double distanceInKM = Double.parseDouble(dist) / 1000;
            //Log.d(TAG, "test :"+ dist);
            long timemins = getFormattedTime();
            mdistance.setText("Distance: " + dist + "m");
            paceVal = ((double) timemins /60) / distanceInKM;
            mpace.setText(String.format("%.2f", paceVal) + "min/km");

        }
    }
    }


