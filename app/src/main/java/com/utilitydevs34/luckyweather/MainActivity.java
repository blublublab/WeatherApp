package com.utilitydevs34.luckyweather;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
//http://api.openweathermap.org/data/2.5/weather?q=Kiev&lang=ru&units=metric&appid=849ae2dfafc
////http://api.openweathermap.org/data/2.5/weather?q=Kiev&lang=ru&units=metric&appid=849ae2dfafc5ee24445547ce32c25f0a
public class MainActivity extends AppCompatActivity {
    public static WeakReference<MainActivity> weakActivity;
    private TextView textViewLatLon;
    private EditText editTextNameOfCity;
    private TextView textViewWeather;
    private ImageView imageViewTypeOfWeather;
    private Button buttonCheckWeather;
    private LocationGPS locationGPS;
    double[] locationGPSArray = new double[2];
    private final int WAITING_TIME = 5;

    // weak reference to main activity
    public static MainActivity getmInstanceActivity() {
        return weakActivity.get();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        weakActivity = new WeakReference<>(MainActivity.this);
        editTextNameOfCity = findViewById(R.id.editTextNameOfCity);
        textViewWeather = findViewById(R.id.textViewWeather);
        imageViewTypeOfWeather = findViewById(R.id.imageViewTypeOfWeather);
        buttonCheckWeather = findViewById(R.id.buttonCheckWeather);
        textViewLatLon = findViewById(R.id.textViewLatLon);


        locationGPS = new LocationGPS();
        Location location;
        location = locationGPS.updatePGS();
        //Handler
        if (location != null) {
            locationGPSArray[0] = location.getLatitude();
            locationGPSArray[1] = location.getLongitude();
            getWeather(locationGPSArray);
        } else {
            Log.i("location", "location is Null 71 MainActivity");

        }
    }

    public void getWeather(double[] locationGPSArray) {
        String url;
        if (locationGPSArray[0] != 0.0 || locationGPSArray[1] != 0.0) {
            editTextNameOfCity.setVisibility(View.INVISIBLE);
            buttonCheckWeather.setVisibility(View.INVISIBLE);
            String location = "lat=" + locationGPSArray[0] + "&lon=" + locationGPSArray[1];
            url = "https://api.openweathermap.org/data/2.5/weather?" + location + "&lang=en&units=metric&appid=849ae2dfafc5ee24445547ce32c25f0a";
            Log.i("location", url);
        } else {

            String city = editTextNameOfCity.getText().toString().trim();
            try {
                city = URLEncoder.encode(city, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&lang=en&units=metric&appid=849ae2dfafc5ee24445547ce32c25f0a";
        }
        GetWeatherTask getWeatherTask = new GetWeatherTask();
        try {
            Weather weather;
            weather = getWeatherTask.execute(url, getString(R.string.enter_correct_city_text), getString(R.string.now_text)).get();
            if (weather.getIsItCorrect()) {
                // ?????? ?????? ???????? ?????? ??????????????????
                imageViewTypeOfWeather.setImageBitmap(weather.getIcon());
                textViewWeather.setText(String.format( Locale.getDefault() , "%s%s%s%d%s%d%s%s%s",  getString(R.string.weather_now_text), weather.getTypeOfWeather(), getString(R.string.tempnowtext), (int) weather.getTemp(), getString(R.string.feelingliketext), (int) weather.getFeelsLikeTemp(), getString(R.string.speedwindtext), weather.getWindSpeed(), getString(R.string.mstext)));
            } else {
                textViewWeather.setText(getString(R.string.city_not_found));
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void onClickGetWeather(View view) {
        getWeather(locationGPSArray);
    }

    public void onClickPrivacyPolicy(View view) {
        Intent intentPriv = new Intent(this, GPSTopActivity.class);
        intentPriv.putExtra("privpo", "privpol");
        startActivity(intentPriv);
    }

    public static class GetWeatherTask extends AsyncTask<String, Void, Weather> {

        private final Weather weather = new Weather();
        private InputStream inputStream = null;
        private final StringBuilder readingJson = new StringBuilder();
        private InputStream inputStreamIcon = null;

        @Override
        protected Weather doInBackground(String... strings) {
            try {

                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    readingJson.append(line);
                }
                JSONObject jsonAllInfo = new JSONObject(readingJson.toString());

                if (jsonAllInfo.has("message")) {
                    weather.setItCorrect(false);
                    return weather;
                } else {
                    weather.setItCorrect(true);
                    JSONArray jsonWeather = jsonAllInfo.getJSONArray("weather");
                    // ???????????????????? ???????????? ????????????
                    URL urlOfIconDownload = new URL("https://openweathermap.org/img/wn/" + jsonWeather.getJSONObject(0).getString("icon") + "@2x.png");
                    HttpURLConnection urlConnectionIcon = (HttpURLConnection) urlOfIconDownload.openConnection();
                    inputStreamIcon = urlConnectionIcon.getInputStream();
                    Bitmap icon = BitmapFactory.decodeStream(inputStreamIcon);
                    weather.setIcon(icon);
                    weather.setTypeOfWeather(jsonWeather.getJSONObject(0).getString("description"));
                    weather.setTemp((Double) jsonAllInfo.getJSONObject("main").get("temp"));
                    weather.setFeelsLikeTemp((Double) jsonAllInfo.getJSONObject("main").get("feels_like"));
                    weather.setWindSpeed(jsonAllInfo.getJSONObject("wind").getDouble("speed"));
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (inputStreamIcon != null) {
                    try {
                        inputStreamIcon.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return weather;
        }
    }


}