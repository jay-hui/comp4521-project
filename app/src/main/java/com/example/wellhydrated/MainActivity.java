package com.example.wellhydrated;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private int waterCount = 0;
    private TextView labelWaterAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        labelWaterAmount = findViewById(R.id.label_water_amount);
        Log.d("MainActivity", "main activity started");
    }

    public void drinkWater(View view) {
        waterCount++;
        String newText = "You drank " + waterCount + " cups of water today!";
        labelWaterAmount.setText(newText);

        Toast toast = Toast.makeText(this, R.string.toast_drink_water, Toast.LENGTH_SHORT);
        toast.show();
    }
}