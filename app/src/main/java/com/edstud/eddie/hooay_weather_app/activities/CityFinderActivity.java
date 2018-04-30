package com.edstud.eddie.hooay_weather_app.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.edstud.eddie.hooay_weather_app.R;
import com.edstud.eddie.hooay_weather_app.model.CityResult;
import com.edstud.eddie.hooay_weather_app.provider.YahooClient;

import java.util.ArrayList;
import java.util.List;

public class CityFinderActivity extends Activity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cityresult_layout);
        AutoCompleteTextView edt = this.findViewById(R.id.edtCity);
        CityAdapter adapter = new CityAdapter(this, null);
        edt.setAdapter(adapter);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        edt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CityResult result = (CityResult) parent.getItemAtPosition(position);
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(CityFinderActivity.this);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("woeid", result.getWoeid());
                editor.putString("cityName", result.getCityName());
                editor.putString("country", result.getCountry());
                editor.commit();

                NavUtils.navigateUpFromSameTask(CityFinderActivity.this);
            }
        });
    }

    private class CityAdapter extends ArrayAdapter<CityResult> implements Filterable{
        private Context context;
        private List<CityResult> cityList = new ArrayList<CityResult>();

        public CityAdapter(Context context, List<CityResult> cityList){
            super(context, R.layout.cityresult_layout, cityList);
            this.cityList = cityList;
            this.context = context;
        }

        @Nullable
        @Override
        public CityResult getItem(int position) {
            if (cityList != null)
                return cityList.get(position);
            return null;
        }

        @Override
        public int getCount(){
            if (cityList != null)
                return cityList.size();
            return 0;
        }

        @Override
        public long getItemId(int position) {
            if (cityList != null){
                return cityList.get(position).hashCode();
            }
            return 0;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View result = convertView;

            if (result == null){
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                result = inflater.inflate(R.layout.cityresult_layout, parent, false);
            }

            TextView tv = result.findViewById(R.id.txtCityName);
            tv.setText(cityList.get(position).getCityName() + "," + cityList.get(position).getCountry());

            return result;
        }

        @NonNull
        @Override
        public Filter getFilter() {
            //retreive data from a remote server by implementing Filterable interface in this Adapter => get the result
            Filter cityFilter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    //this method runs in a seperate thread so no problems should be existed.
                    FilterResults results = new FilterResults();
                    if (constraint == null || constraint.length() < 2) return results;

                    //make the HTTP call and retrieve the data
                    List<CityResult> cityResultList = YahooClient.getCityList(constraint.toString());
                    results.values = cityResultList;
                    results.count = cityResultList.size();

                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    cityList = (List) results.values;
                    notifyDataSetChanged();
                }
            };
            return cityFilter;
        }
    }
}
