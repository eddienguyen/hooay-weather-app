package com.edstud.eddie.hooay_weather_app.provider;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.edstud.eddie.hooay_weather_app.Config;
import com.edstud.eddie.hooay_weather_app.model.CityResult;
import com.edstud.eddie.hooay_weather_app.model.Weather;
import com.survivingwithandroid.weather.lib.WeatherClient;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.android.volley.Request.*;
import static com.android.volley.Request.Method.*;

// A parser class that we call YahooClient.
// This class is in charge of retrieving XML data and parse it
public class YahooClient {

    public static String YAHOO_GEO_URL = "http://where.yahooapis.com/v1";
    public static String YAHOO_WEATHER_URL = "hhttp://weather.yahooapis.com/forecastrss";

    private static String APPID = "APP_ID_KEY";

    public static List<CityResult> getCityList(String cityName) {
        List<CityResult> result = new ArrayList<CityResult>();
        HttpURLConnection yahooHttpConn = null;

        String query = makeQueryCityURL(cityName);
        try {
            yahooHttpConn = (HttpURLConnection) (new URL(query)).openConnection();
            yahooHttpConn.connect();
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new InputStreamReader(yahooHttpConn.getInputStream()));
            Log.d("Hooay", "XML parser ok");

            int event = parser.getEventType();

            CityResult cty = null;
            String tagName = null;
            String currentTag = null;

            //START PARSING THE URL:
            // Simply get the first XML event (event is declared above)
            // and we start traversing the XML document until we reach the end.
            // At the method end, we have a list of cities with the woied that is the information we were looking for.
            while (event != XmlPullParser.END_DOCUMENT) {
                tagName = parser.getName();

                if (event == XmlPullParser.START_TAG) {
                    if (tagName.equals("place")) {
                        //place Tag found => create a new CityResult
                        cty = new CityResult();
                    }
                    currentTag = tagName;
                } else if (event == XmlPullParser.TEXT) {
                    //some text => check the tagName to know the tag is related to the text
                    if ("woeid".equals(currentTag))
                        cty.setWoeid(parser.getText());
                    else if ("name".equals(currentTag))
                        cty.setCityName(parser.getText());
                    else if ("country".equals(currentTag))
                        cty.setCountry(parser.getText());

                    //Other tag at the moment is no need to analyzed
                } else if (event == XmlPullParser.END_TAG) {
                    if ("place".equals(tagName)) result.add(cty);
                }
                event = parser.next();
            }

        } catch (Throwable t) {
            Log.e("Error in getCityList", t.getMessage());
            t.printStackTrace();
        } finally {
            try {
                yahooHttpConn.disconnect();
            } catch (Throwable t) {
                //just ignore the Throwable :D
            }
        }
        return result;
    }

    public static void getWeather(String woeid, String unit, RequestQueue rq, final WeatherClientListener listener) {
        String url2Call = makeWeatherURL(woeid, unit);

        final Weather result = new Weather();

        StringRequest request = new StringRequest(Method.GET, url2Call, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                parseResponse(response, result);
                    listener.onWeatherResponse(result);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        rq.add(request);
    }

    private static com.edstud.eddie.hooay_weather_app.model.Weather parseResponse(String response, com.edstud.eddie.hooay_weather_app.model.Weather result) {
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new StringReader(response));

            String tagName = null;
            String currentTag = null;

            int event = parser.getEventType();
            boolean isFirstDayForecast = true;

            while (event != XmlPullParser.END_DOCUMENT) {
                tagName = parser.getName();

                if (event == XmlPullParser.START_TAG) {
                    if (tagName.equals("yweather:wind")) {
                        result.wind.chill = Integer.parseInt(parser.getAttributeValue(null, "chill"));
                        result.wind.direction = Integer.parseInt(parser.getAttributeValue(null, "direction"));
                        result.wind.speed = (int) Float.parseFloat(parser.getAttributeValue(null, "speed"));
                    } else if (tagName.equals("yweather:atmosphere")) {
                        result.atmosphere.humidity = Integer.parseInt(parser.getAttributeValue(null, "humidity"));
                        result.atmosphere.visibility = Float.parseFloat(parser.getAttributeValue(null, "visibility"));
                        result.atmosphere.pressure = Float.parseFloat(parser.getAttributeValue(null, "pressure"));
                        result.atmosphere.rising = Integer.parseInt(parser.getAttributeValue(null, "rising"));
                    } else if (tagName.equals("yweather:forecast")) {
                        if (isFirstDayForecast) {
                            result.forecast.code = Integer.parseInt(parser.getAttributeValue(null, "code"));
                            result.forecast.tempMin = Integer.parseInt(parser.getAttributeValue(null, "low"));
                            result.forecast.tempMax = Integer.parseInt(parser.getAttributeValue(null, "high"));
                            isFirstDayForecast = false;
                        }
                    } else if (tagName.equals("yweather:condition")) {
                        result.condition.code = Integer.parseInt(parser.getAttributeValue(null, "code"));
                        result.condition.description = parser.getAttributeValue(null, "text");
                        result.condition.temp = Integer.parseInt(parser.getAttributeValue(null, "temp"));
                        result.condition.date = parser.getAttributeValue(null, "date");
                    } else if (tagName.equals("yweather:units")) {
                        result.units.temperature = "Â°" + parser.getAttributeValue(null, "temperature");
                        result.units.pressure = parser.getAttributeValue(null, "pressure");
                        result.units.distance = parser.getAttributeValue(null, "distance");
                        result.units.speed = parser.getAttributeValue(null, "speed");
                    } else if (tagName.equals("yweather:location")) {
                        result.location.name = parser.getAttributeValue(null, "city");
                        result.location.region = parser.getAttributeValue(null, "region");
                        result.location.country = parser.getAttributeValue(null, "country");
                    } else if (tagName.equals("image")) {
                        currentTag = "image";
                    } else if (tagName.equals("url")) {
                        result.imageUrl = parser.getAttributeValue(null, "src");
                    } else if (tagName.equals("lastBuildDate")) {
                        currentTag = "update";
                    } else if (tagName.equals("yweather:astronomy")) {
                        result.astronomy.sunrise = parser.getAttributeValue(null, "sunrise");
                        result.astronomy.sunset = parser.getAttributeValue(null, "sunset");
                    }
                } else if (event == XmlPullParser.END_TAG) {
                    if ("image".equals(currentTag)) {
                        currentTag = null;
                    }
                } else if (event == XmlPullParser.TEXT) {
                    if ("update".equals(currentTag)) {
                        result.lastUpdate = parser.getText();
                    }
                    event = parser.next();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return result;
    }

    private static String makeQueryCityURL(String cityName) {
        //remove spaces in cityName:
        cityName = cityName.replaceAll(" ", "%20");
        return YAHOO_GEO_URL + "/places.q(" + cityName + "%2A);count=" + Config.MAX_CITY_RESULT + "?appid=" + APPID;
    }

    private static String makeWeatherURL(String woeid, String unit) {
        return YAHOO_WEATHER_URL + "?w=" + woeid + "&u=" + unit;
    }

    //    Public Listener interface
    public static interface WeatherClientListener {
        public void onWeatherResponse(Weather weather);
    }
}
