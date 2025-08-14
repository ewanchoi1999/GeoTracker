package com.choi.geotracker;


import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

/**
 * Recycler view class to display workouts
 */

public class WorkoutView extends AppCompatActivity {
    private RecyclerView recyclerView;
    private static final String TAG = "Workout";
    private WorkoutViewModel viewModel;

    /**
     * Attaches viewmodel to recyclerview, and observes changes (i.e. once workout is finished) and displays the new workouts
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workout_view);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.workouts);
        View root = findViewById(R.id.root_w);
        Snackbar.make(root, "Swipe on the card to delete a single workout", Snackbar.LENGTH_SHORT).show();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(true);
        final WorkoutViewAdapter adapter = new WorkoutViewAdapter();
        viewModel = new ViewModelProvider(this).get(WorkoutViewModel.class);
        /**
         * Gets all workouts and displays on the screen
         */
        viewModel.getAllWorkouts().observe(this, new Observer<List<TrackData>>() {
            @Override
            public void onChanged(List<TrackData> trackData) {
                adapter.submitList(trackData);
            }
        });
        recyclerView.setAdapter(adapter);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.workouts) {
                return true;
            } else if (itemId == R.id.home) {
                Intent toHome = new Intent(WorkoutView.this, MainActivity.class);
                startActivity(toHome);
                finish();

                return true;
            }
            return false;
        });

        /**
         * Item Touch Helper to allow user to swipe to delete
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                viewModel.delete(adapter.getItemAtPosition(viewHolder.getAdapterPosition()));
                Toast.makeText(WorkoutView.this, "Workout deleted", Toast.LENGTH_SHORT).show();
                            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addBackgroundColor(ContextCompat.getColor(WorkoutView.this, R.color.red))
                        .addActionIcon(R.drawable.workout_delete)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new WorkoutViewAdapter.onItemClick() {
            @Override
            public void onWorkoutItemClick(TrackData data) {
                Toast.makeText(WorkoutView.this, "Liked a workout!", Toast.LENGTH_SHORT).show();
            }
        });

        /**
         * Retrieves the workout from the tracking UI
         */
        Intent intent = getIntent();
        if (getIntent().hasExtra("time") && getIntent().hasExtra("date") && getIntent().hasExtra("pace")
                && getIntent().hasExtra("distance") && getIntent().hasExtra("type")) {
            String workoutTime = getIntent().getStringExtra("time");
            String workoutDate = getIntent().getStringExtra("date");
            String workoutPace = getIntent().getStringExtra("pace");
            String workoutDistance = getIntent().getStringExtra("distance");
            String workoutType = getIntent().getStringExtra("type");

            TrackData trackData = new TrackData(workoutDistance, workoutDate, workoutPace, workoutType, workoutTime);
            viewModel.insert(trackData);
        } else {
            Log.d(TAG, "No intents passed");
        }
    }

    /**
     * Inflates the menu to provide users an option to delete all workouts
     * @param menu The options menu in which you place your items.
     *
     * @return
     */
    @Override
        public boolean onCreateOptionsMenu (Menu menu) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.deletallworkouts, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == R.id.deleteAllWorkouts) {
                viewModel.deleteAll();
                Toast.makeText(WorkoutView.this, "All workouts deleted", Toast.LENGTH_SHORT).show();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

    }




