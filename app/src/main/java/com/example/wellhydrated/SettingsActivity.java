package com.example.wellhydrated;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    public static final String KEY_PREF_THEME = "theme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String themePref = sharedPref.getString(SettingsActivity.KEY_PREF_THEME, "0");
        Toast.makeText(this, themePref, Toast.LENGTH_SHORT).show();
    }
}