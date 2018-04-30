package com.edstud.eddie.hooay_weather_app.activities;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.widget.Toolbar;

import com.edstud.eddie.hooay_weather_app.R;

public class WeatherPreferenceActivity extends PreferenceActivity {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setActionBar(mToolbar);
        if (getActionBar() != null){
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        String action = getIntent().getAction();

        addPreferencesFromResource(R.xml.weather_prefs);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //set current value in the description
        Preference prefLocation = getPreferenceScreen().findPreference("edstud_location");
        Preference prefTemp = getPreferenceScreen().findPreference("edstud_temp_unit");

        prefLocation.setSummary(getResources().getText(R.string.summary_location) + " " + prefs.getString("cityName",null)
            + " " + prefs.getString("country", null));

        String unit = prefs.getString("edstud_temp_unit", null) != null ? "°" + prefs.getString("edstud_temp_unit", null)
                .toUpperCase() : "";

        prefTemp.setSummary(getResources().getText(R.string.summary_temp) + " " + unit);

    }
}
