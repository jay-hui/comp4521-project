package com.example.wellhydrated;

import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
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

    private int cupsOfWaterLeft;
    private TextView labelWaterAmount;

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
        //waterView = findViewById(R.id.water_view);
        //drowningManView = findViewById(R.id.drowning_man_view);
        dbHelper = new DBHelper(getApplicationContext());
        db = dbHelper.getWritableDatabase();
        cupsOfWaterLeft = calCupsOfWaterLeft();

        fillEmptyRecords();
    }

    public String getHomeInfo() {
        Log.d("MainActivity", "getHomeInfo");
        return String.format(getResources().getString(R.string.label_water_left), cupsOfWaterLeft);
    }

    public int getCupsOfWaterLeft() {
        return cupsOfWaterLeft;
    }

    public void drinkWater(View view) {
        Log.d("MainActivity", "drinkWater");

        //waterView = findViewById(R.id.water_view);
        //drowningManView = findViewById(R.id.drowning_man_view);

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
        Log.d("DB","One record inserted for " + currentDate + " " + currentTime);
        cupsOfWaterLeft = calCupsOfWaterLeft();

        waterLevelRise(1);
        /*
        ConstraintLayout homeLayout = findViewById(R.id.home_layout);

        if (cupsOfWaterLeft > 0) {
            if (waterView.getTranslationY() > homeLayout.getHeight())
                waterView.setTranslationY(homeLayout.getHeight() - 300);
            ObjectAnimator anim = ObjectAnimator.ofFloat(waterView, view.TRANSLATION_Y, waterView.getTranslationY(), cupsOfWaterLeft == 1 ? 0f : waterView.getTranslationY() - (homeLayout.getHeight() * 0.1f));
            anim.setDuration(500);
            anim.setInterpolator(new LinearInterpolator());
            anim.start();

            anim = ObjectAnimator.ofFloat(drowningManView, view.TRANSLATION_Y, drowningManView.getTranslationY(), cupsOfWaterLeft == 1 ? -2000f : drowningManView.getTranslationY() - (homeLayout.getHeight() * 0.1f));
            anim.setDuration(500);
            anim.setInterpolator(new LinearInterpolator());
            anim.start();
            cupsOfWaterLeft--;
        }
        */
        labelWaterAmount = findViewById(R.id.label_water_amount);
        labelWaterAmount.setText(getHomeInfo());
        Toast toast = Toast.makeText(this, String.format(getResources().getString(R.string.toast_drink_water), 250), Toast.LENGTH_SHORT);
        toast.show();

    }

    public void fillEmptyRecords() {
        Calendar date = Calendar.getInstance();
        // Check the past 365 days
        for (int i= 1; i <= 364; i++) {
            date.add(Calendar.DATE, -1);

            int year = date.get(Calendar.YEAR);
            String month = String.valueOf(date.get(Calendar.MONTH) + 1); // It ranges from 0 - 11
            String day = String.valueOf(date.get(Calendar.DAY_OF_MONTH));

            // "1" to "01", "5" to "05", etc
            if (month.length() == 1) month = "0" + month;
            if (day.length() == 1) day = "0" + day;

            String pastDate = String.format("%d-%s-%s", year, month, day);

            String query = "SELECT * " +
                    "FROM " + WellHydratedDBEntries.TABLE_NAME + " " +
                    "WHERE " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE + " = '" + pastDate + "'";
            Cursor cursor = db.rawQuery(query, null);

            if (cursor.getCount() == 0) { // No records found => The user skipped that day
                ContentValues values = new ContentValues();
                values.put(WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE, pastDate);
                // drink_time = NULL
                values.put(WellHydratedDBEntries.COLUMN_NAME_AMOUNT, 0);

                db.insert(WellHydratedDBEntries.TABLE_NAME, null, values);
                Log.d("DB","One empty record inserted for " + pastDate);
            }

        }
    }

    public int calCupsOfWaterLeft() {
        //SELECT *
        //FROM wellhydrated_records
        //WHERE drinkdate = (SELECT DATE('now', 'localtime')) AND amount <> 0
        String query = "SELECT * FROM " + WellHydratedDBEntries.TABLE_NAME + " " +
                       "WHERE " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE + " =  (SELECT DATE('now', 'localtime')) AND " + WellHydratedDBEntries.COLUMN_NAME_AMOUNT + " <> 0";
        int cupsDrunk = db.rawQuery(query, null).getCount();

        if (cupsDrunk > 8)
            return 0;
        else
            return 8 - cupsDrunk;
    }

    public void waterLevelRise(int n) {
        ConstraintLayout homeLayout = findViewById(R.id.home_layout);
        View waterView = findViewById(R.id.water_view);
        View drowningManView = findViewById(R.id.drowning_man_view);

        Log.d("homeLayout Height", String.valueOf(homeLayout.getHeight()));
        if (waterView.getTranslationY() > homeLayout.getHeight()) waterView.setTranslationY(homeLayout.getHeight() - 300);

        Log.d("waterView Y", String.valueOf(waterView.getTranslationY()));
        ObjectAnimator anim = ObjectAnimator.ofFloat(waterView, waterView.TRANSLATION_Y, waterView.getTranslationY(), cupsOfWaterLeft == 0 ? 0f : waterView.getTranslationY() - (homeLayout.getHeight() * 0.1f * n));
        anim.setDuration(500);
        anim.setInterpolator(new LinearInterpolator());
        anim.start();

        Log.d("drowningManView Y", String.valueOf(drowningManView.getTranslationY()));
        anim = ObjectAnimator.ofFloat(drowningManView, drowningManView.TRANSLATION_Y, drowningManView.getTranslationY(), cupsOfWaterLeft == 0 ? -2000f : drowningManView.getTranslationY() - (homeLayout.getHeight() * 0.1f * n));
        anim.setDuration(500);
        anim.setInterpolator(new LinearInterpolator());
        anim.start();
    }
}