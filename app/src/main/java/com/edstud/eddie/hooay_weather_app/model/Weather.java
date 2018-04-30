package com.edstud.eddie.hooay_weather_app.model;

public class Weather {
    public String imageUrl;

    public Condition condition = new Condition();
    public Wind wind = new Wind();
    public Atmosphere atmosphere = new Atmosphere();
    public Forecast forecast = new Forecast();
    public Location location = new Location();
    public Astronomy astronomy = new Astronomy();
    public Units units = new Units();

    public String lastUpdate;

    public class Condition{
        public String description;
        public int code;
        public String date;
        public int temp;
    }

    public class Forecast{
        public int tempMin, tempMax;
        public String description;
        public int code;
    }

    public static class Atmosphere{
        public int humidity;
        public float visibility, pressure;
        public int rising;
    }

    public class Wind{
        public int chill, direction, speed;
    }

    public class Units{
        public String speed, distance, pressure, temperature;
    }

    public class Location{
        public String name, region, country;
    }

    public class Astronomy{
        public String sunrise, sunset;
    }
}
