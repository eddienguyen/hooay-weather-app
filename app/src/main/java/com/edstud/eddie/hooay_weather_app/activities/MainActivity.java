package com.edstud.eddie.hooay_weather_app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.edstud.eddie.hooay_weather_app.R;
import com.edstud.eddie.hooay_weather_app.model.Weather;
import com.edstud.eddie.hooay_weather_app.provider.YahooClient;

public class MainActivity extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener {
    private static String TAG = MainActivity.class.getSimpleName();

    private Toolbar mToolbar;
    FragmentDrawer drawerFragment;
    private TextView txtCity;
    private SharedPreferences prefs;
    private RequestQueue requestQueue;

    TextView lineTxt, temp, tempUnit, tempMin, tempMax, windDetailData, humidityDetailData, pressureDetailData,
            visibilityDetailData, sunriseDetailData, sunsetDetailData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        txtCity = (TextView) mToolbar.findViewById(R.id.txtCity);
        lineTxt = findViewById(R.id.lineTxt);
        temp = findViewById(R.id.temp);
        tempUnit = findViewById(R.id.tempUnit);
        tempMin = findViewById(R.id.tempMin);
        tempMax = findViewById(R.id.tempMax);
        windDetailData = findViewById(R.id.windDetailData);
        humidityDetailData = findViewById(R.id.humidityDetailData);
        pressureDetailData = findViewById(R.id.pressureDetailData);
        visibilityDetailData = findViewById(R.id.visibilityDetailData);
        sunriseDetailData = findViewById(R.id.sunriseDetailData);
        sunsetDetailData = findViewById(R.id.sunsetDetailData);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        drawerFragment = (FragmentDrawer) getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        refreshData();
    }

    private void refreshData() {
        if (prefs == null) return;

        String woeid = prefs.getString("woeid", null);

        if (woeid != null) {
            String loc = prefs.getString("cityName", null) + "," + prefs.getString("country", null);
            final String unit = prefs.getString("edstud_temp_unit", null);

            YahooClient.getWeather(woeid, unit, requestQueue, new YahooClient.WeatherClientListener() {
                @Override
                public void onWeatherResponse(Weather weather) {
                    //Update view
                    int code = weather.condition.code;

                    lineTxt.setText(weather.condition.description);
                    temp.setText(weather.condition.temp);

                    int resId = getResource(weather.units.temperature, weather.condition.temp);
                    tempUnit.setText(weather.units.temperature);
                    tempMin.setText("" + weather.forecast.tempMin + " " + weather.units.temperature);
                    tempMax.setText("" + weather.forecast.tempMax + " " + weather.units.temperature);



                }
            });
        }
    }

    private float convertToC(String unit, float val) {
        if (unit.equalsIgnoreCase("Â°C"))
            return val;

        return (float) ((val - 32) / 1.8);
    }


    private int getResource(String unit, float val) {
        float temp = convertToC(unit, val);
        Log.d("SwA", "Temp [" + temp + "]");
        int resId = 0;
        if (temp < 10) {
//            resId = R.drawable.line_shape_blue;
        } else if (temp >= 10 && temp <= 24) {
//            resId = R.drawable.line_shape_green;
        } else if (temp > 25) {
//            resId = R.drawable.line_shape_red;
        }


        return resId;

    }


    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        if (id == R.id.action_settings)


        return super.onOptionsItemSelected(item);
    }

    private void displayView(int position) {
        Fragment fragment = null;
        String title = getString(R.string.location_title);
        switch (position) {
            case 0:
                fragment = new AddFragment();
                title = getString(R.string.title_add);
                break;
            case 1:
                fragment = new EditFragment();
                title = getString(R.string.title_edit);
                break;
            case 2:
                fragment = new TodayFragment();
                title = getString(R.string.title_today);
                break;
            case 3:
                fragment = new ShareFragment();
                title = getString(R.string.title_share);
                break;
            case 4:
                fragment = new SettingsFragment();
                title = getString(R.string.title_settings);
                break;
            case 5:
                fragment = new AboutFragment();
                title = getString(R.string.title_about);
                break;
            default:
                break;
        }

        //switching fragments
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();

            //set toolbar Title
            getSupportActionBar().setTitle(title);
        }
    }


}
