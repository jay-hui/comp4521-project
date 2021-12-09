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

    private int cupsOfWaterLeft = 8;
    private TextView labelWaterAmount;

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
    }

}