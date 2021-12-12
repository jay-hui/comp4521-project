package com.example.wellhydrated;

import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private int cupsOfWaterLeft = 8;
    private TextView labelWaterAmount;
    View waterView;

    protected DBHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "onCreate");
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment,
                R.id.statisticsFragment,
                R.id.settingsFragment
        ).build();
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Initialization can only be done in/after onCreate()
        dbHelper = new DBHelper(getApplicationContext());
        db = dbHelper.getWritableDatabase();

    }

    public String getHomeInfo() {
        Log.d("MainActivity", "getHomeInfo");
        return String.format(getResources().getString(R.string.label_water_left), cupsOfWaterLeft);
    }

    public void drinkWater(View view) {
        Log.d("MainActivity", "drinkWater");

        waterView = findViewById(R.id.water_view);
        ConstraintLayout homeLayout = findViewById(R.id.home_layout);

        if (cupsOfWaterLeft > 0) {
            if (waterView.getTranslationY() > homeLayout.getHeight())
                waterView.setTranslationY(homeLayout.getHeight());
            ObjectAnimator anim = ObjectAnimator.ofFloat(waterView, view.TRANSLATION_Y, waterView.getTranslationY(), waterView.getTranslationY() - (homeLayout.getHeight() * 0.125f));
            anim.setDuration(500);
            anim.setInterpolator(new LinearInterpolator());
            anim.start();

            cupsOfWaterLeft--;
        }
        labelWaterAmount = findViewById(R.id.label_water_amount);
        labelWaterAmount.setText(getHomeInfo());
        Toast toast = Toast.makeText(this, R.string.toast_drink_water, Toast.LENGTH_SHORT);
        toast.show();

        Date currentDateTime = new Date();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentDateTime);
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(currentDateTime);

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE, currentDate);
        values.put(WellHydratedDBEntries.COLUMN_NAME_DRINK_TIME, currentTime);
        values.put(WellHydratedDBEntries.COLUMN_NAME_AMOUNT, 250); // hard-coded to 250 temporarily

        // Insert a record into the Database
        db.insert(WellHydratedDBEntries.TABLE_NAME, null, values);
        Log.d("DB","One record inserted");
    }

    public void updateGraph0(View view) {
        StatisticsFragment statsFragment = (StatisticsFragment) getSupportFragmentManager().findFragmentById(R.id.statisticsFragment);
        if (statsFragment == null) {
            Log.d("OMG", "NULLLLLLLLL");
        }
        statsFragment.updateGraph(0);
    }

    public void updateGraph1(View view) {
        StatisticsFragment statsFragment = (StatisticsFragment) getSupportFragmentManager().findFragmentById(R.id.statisticsFragment);
        statsFragment.updateGraph(1);
    }

    public void updateGraph2(View view) {
        StatisticsFragment statsFragment = (StatisticsFragment) getSupportFragmentManager().findFragmentById(R.id.statisticsFragment);
        statsFragment.updateGraph(2);
    }
}