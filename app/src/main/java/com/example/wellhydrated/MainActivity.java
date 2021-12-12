package com.example.wellhydrated;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private int cupsOfWaterLeft = 8;
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
        if (cupsOfWaterLeft > 0) cupsOfWaterLeft--;
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

    public void updateGraph(int viewMode) {
        String query;
        // SQL Queries:

        //===================================Past 7 Days==================================================
        //SELECT drink_date, SUM(amount) AS total_amount
        //FROM wellhydrated_records
        //WHERE drink_date BETWEEN (SELECT date("now", "-6 day")) AND (SELECT date("now"))
        //GROUP BY drink_date
        //ORDER BY drink_date
        //================================================================================================

        //===================================Past 30 Days=================================================
        //SELECT drink_date, SUM(amount) AS total_amount
        //FROM wellhydrated_records
        //WHERE drink_date BETWEEN (SELECT date("now", "-29 day")) AND (SELECT date("now"))
        //GROUP BY drink_date
        //ORDER BY drink_date
        //================================================================================================

        //===================================Past 12 Months===============================================
        //SELECT strftime("%Y-%m", drink_date) AS ym, avg(total_amount)
        //FROM (SELECT drink_date, SUM(amount) AS total_amount
        //      FROM wellhydrated_records
        //      WHERE drink_date BETWEEN (SELECT date("now", "-11 month", "start of month")) AND (SELECT date("now"))
        //      GROUP BY drink_date)
        //GROUP BY ym
        //ORDER BY ym
        //================================================================================================

        // Pick the right SQL query first
        if (viewMode == 0) {
            // Daily amount drunk in the past 7 days
            query = "SELECT " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE + ", SUM(" + WellHydratedDBEntries.COLUMN_NAME_AMOUNT + ") AS total_amount " +
                    "FROM " + WellHydratedDBEntries.TABLE_NAME + " " +
                    "WHERE " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE + " BETWEEN (SELECT date('now', 'localtime', '-6 day')) AND (SELECT date('now', 'localtime')) " +
                    "GROUP BY " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE + " " +
                    "ORDER BY " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE;

        } else if (viewMode == 1) {
            // Daily amount drunk in the past 30 days
            query = "SELECT " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE + ", SUM(" + WellHydratedDBEntries.COLUMN_NAME_AMOUNT + ") AS total_amount " +
                    "FROM " + WellHydratedDBEntries.TABLE_NAME + " " +
                    "WHERE " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE + " BETWEEN (SELECT date('now', 'localtime', '-29 day')) AND (SELECT date('now', 'localtime')) " +
                    "GROUP BY " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE + " " +
                    "ORDER BY " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE;

        } else if (viewMode == 2) {
            // Average of daily amount for each months in the past 12 months
            query = "SELECT strftime('%Y-%m', "+ WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE + ") AS ym, avg(total_amount) " +
                    "FROM (SELECT " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE + ", SUM(" + WellHydratedDBEntries.COLUMN_NAME_AMOUNT + ") AS total_amount " +
                    "      FROM " + WellHydratedDBEntries.TABLE_NAME +
                    "      WHERE " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE + " BETWEEN (SELECT date('now', 'localtime', '-11 month', 'start of month')) AND (SELECT date('now', 'localtime')) " +
                    "      GROUP BY " + WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE + ") " +
                    "GROUP BY ym " +
                    "ORDER BY ym";
        } else
            // Invalid viewMode
            return;

        Cursor cursor = db.rawQuery(query, null);
        int cursorSize = cursor.getCount();

        Log.d("DB", "Retrieved " + String.valueOf(cursorSize) + " rows");

        // Assumed cursorSize != 0
        DataPoint[] dataPoints = new DataPoint[cursorSize];
        String[] xLabels = new String[cursorSize];

        int x = 0;
        while (cursor.moveToNext()) {
            String label = cursor.getString(0);
            xLabels[x] = label;

            int y = cursor.getInt(1);
            dataPoints[x] = new DataPoint(x, y);

            x++;
        }
        cursor.close();

        GraphView graph = (GraphView) findViewById(R.id.statsGraph);

        // Remove the previous graph, if any (?)
        graph.removeAllSeries();

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoints);
        series.setDrawDataPoints(true);
        series.setAnimated(true);
        series.setOnDataPointTapListener((series1, dataPoint) -> {
            graph.setTooltipText(String.format("%s: %dml", xLabels[(int) dataPoint.getX()], (int) dataPoint.getY()));

            float yAxisSize = (float) graph.getViewport().getMaxYAxisSize();
            float xAxisSize = (float) graph.getViewport().getMaxXAxisSize();
            if (yAxisSize == 0) yAxisSize = (float) series1.getHighestValueY();
            if (xAxisSize == 0) xAxisSize = (float) series1.getHighestValueX();

            // Programmatically trigger the tooltip at an appropriate position
            graph.performLongClick((float) dataPoint.getX() / xAxisSize * (float) graph.getGraphContentWidth(), graph.getBottom() - (float) dataPoint.getY() / yAxisSize * (float) graph.getGraphContentHeight());

        });
        graph.addSeries(series);

        //graph.getGridLabelRenderer().setTextSize(25.0f);
        // TODO: Add titles, other info to the graph

        // Use static labels for horizontal (and vertical) labels
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        staticLabelsFormatter.setHorizontalLabels(xLabels);
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
    }

    public void updateGraph0(View view) {
        updateGraph(0);
    }

    public void updateGraph1(View view) {
        updateGraph(1);
    }

    public void updateGraph2(View view) {
        updateGraph(2);
    }
}