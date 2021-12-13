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
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

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
    ImageView drowningManView;

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

        fillEmptyRecords();
    }

    public String getHomeInfo() {
        Log.d("MainActivity", "getHomeInfo");
        return String.format(getResources().getString(R.string.label_water_left), cupsOfWaterLeft);
    }

    public void drinkWater(View view) {
        Log.d("MainActivity", "drinkWater");

        waterView = findViewById(R.id.water_view);
        drowningManView = findViewById(R.id.drowning_man_view);
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
        Log.d("DB","One record inserted for " + currentDate + " " + currentTime);
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
}