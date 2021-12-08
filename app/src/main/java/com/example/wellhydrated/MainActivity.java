package com.example.wellhydrated;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    private int waterCount = 0;
    private TextView labelWaterAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        labelWaterAmount = findViewById(R.id.label_water_amount);
        Log.d("MainActivity", "main activity started");

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment,
                R.id.statisticsFragment,
                R.id.settingsFragment
        ).build();

        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

    }

    public void getHomeInfo() {
        labelWaterAmount = findViewById(R.id.label_water_amount);
        String newText = "You drank " + waterCount + " cups of water today!";
        labelWaterAmount.setText(newText);
    }

    public void drinkWater(View view) {
        labelWaterAmount = findViewById(R.id.label_water_amount);
        waterCount++;
        String newText = "You drank " + waterCount + " cups of water today!";
        labelWaterAmount.setText(newText);

        Toast toast = Toast.makeText(this, R.string.toast_drink_water, Toast.LENGTH_SHORT);
        toast.show();
    }

}