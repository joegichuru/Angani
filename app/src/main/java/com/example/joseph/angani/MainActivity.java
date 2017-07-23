package com.example.joseph.angani;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    //replace this with your API key. otherwise just use mine
    public static final String API_KEY = "98af75addddba544ec6e050a9c1a572b";
    public static final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?mode=json&";

    RecyclerView recyclerView;
    LocationManager locationManager;
    private boolean locationAccessAllowed=false;
    LocationListener locationListener=new MyLocationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.weather_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},123);


        }
        locationListener=new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,locationListener);
        Location location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        MockLocation mockLocation;
        if(location!=null){
           mockLocation=new MockLocation(location.getLatitude(),location.getLongitude());
        }else {
            mockLocation=new MockLocation(-1.15321,36.91114);
            Toast.makeText(this, "We could not find your exact position.", Toast.LENGTH_SHORT).show();
        }

        new WeeklyWeatherData().execute(mockLocation);
    }

    @Override
    protected void onPause() {
        super.onPause();
       locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode==123){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){

                Toast.makeText(this,"Permission Granted",Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(this,"Permission to access location Denied",Toast.LENGTH_SHORT).show();
            }

        }
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);

    }

    //get weather data of the given latitude and longitude
    private String getWeeklyUpdates(MockLocation mockLocation){
        double lat,lon;
        lat= mockLocation.getLat();
        lon= mockLocation.getLon();
        HttpURLConnection httpURLConnection=null;
        StringBuilder stringBuilder=new StringBuilder();
        InputStream inputStream=null;
        try {
            URL url=new URL(BASE_URL+"lat="+lat+"&lon="+lon+"&cnt=16&appid="+API_KEY);
            httpURLConnection= (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();
            inputStream=httpURLConnection.getInputStream();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line=bufferedReader.readLine())!=null){
                stringBuilder.append(line);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            //always remember to close the url connection resource
            httpURLConnection.disconnect();
        }
        return stringBuilder.toString();
    }
    private class WeeklyWeatherData extends AsyncTask<MockLocation,Void,String>{

        @Override
        protected String doInBackground(MockLocation... params) {
            return getWeeklyUpdates(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            List<Weather> weatherList=new ArrayList<>();
            try {
                JSONObject weeklyWeatherObject=new JSONObject(result);
                JSONObject city=weeklyWeatherObject.getJSONObject("city");
                String cityName=city.getString("name");
                JSONArray weatherdata=weeklyWeatherObject.getJSONArray("list");
                //iterate over the array to get daily updates
                Weather weather=new Weather();
                int z;
                for(int i=0;i<weatherdata.length();i++){
                    z=i;
                    weather=new Weather();
                    JSONObject jsonObject=weatherdata.getJSONObject(i);
                    JSONObject temp=jsonObject.getJSONObject("temp");
                    double high=temp.getDouble("max");
                    //convert to celcious
                    high=high-273.15;
                    double min=temp.getDouble("min");
                    min=min-273.15;
                    JSONArray weath=jsonObject.getJSONArray("weather");
                    JSONObject specWeath=weath.getJSONObject(0);
                    String status=specWeath.getString("main");
                    double cloudCoverage=jsonObject.getInt("clouds");
                    long time=jsonObject.getLong("dt");
                    switch (status){
                        case "Rain":
                            weather.setStatusIcon(R.drawable.art_rain);
                            break;
                        case "Clear":
                            weather.setStatusIcon(R.drawable.art_clear);
                            break;
                        case "Couds":
                            weather.setStatusIcon(R.drawable.art_clouds);
                        default:
                            weather.setStatusIcon(R.drawable.art_light_clouds);
                    }
                    //get todays day of the week
                    Calendar calendar=Calendar.getInstance();
                    calendar.setTimeInMillis(time*1000L);
                    String day=calendar.getDisplayName(Calendar.DAY_OF_WEEK,Calendar.LONG, Locale.getDefault());
                    weather.setTempLow(min);
                    weather.setTempHigh(high);
                    weather.setCityName(cityName);
                    weather.setStatus(status);
                    weather.setDayOfWeek(day);
                    weatherList.add(weather);
                }
                Weather todaysWeather=weatherList.get(0);
                TextView today,city_name,today_high,today_low,today_status;
                ImageView status_icon= (ImageView) findViewById(R.id.today_status_icon);
                today= (TextView) findViewById(R.id.today_date);
                city_name= (TextView) findViewById(R.id.city);
                today_high= (TextView) findViewById(R.id.today_high);
                today_low= (TextView) findViewById(R.id.today_low);
                today_status= (TextView) findViewById(R.id.today_status);

                today.setText("Today, "+todaysWeather.getDayOfWeek());
                city_name.setText(todaysWeather.getCityName());
                today_high.setText(String.valueOf((int)todaysWeather.getTempHigh())+(char)0x00B0);
                today_low.setText(String.valueOf((int)todaysWeather.getTempLow())+(char)0x00B0);
                today_status.setText(todaysWeather.getStatus());
                status_icon.setImageResource(todaysWeather.getStatusIcon());
                recyclerView.setAdapter(new WeatherViewAdapter(MainActivity.this,weatherList));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPostExecute(result);

        }
    }
    private class MockLocation {
        private double lat,lon;

        public MockLocation(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }
    }
    private class MyLocationListener implements LocationListener{

        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}
