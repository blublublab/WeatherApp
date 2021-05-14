package com.demo.weatherapp;

import android.graphics.Bitmap;

public class Weather {
    private Bitmap icon;
    private boolean isItCorrect;
    private double feelsLikeTemp;
    private double temp;
    private String typeOfWeather;
    private double windSpeed;

    public Weather() {
    }

    public Weather(Bitmap icon, boolean isItCorrect, double feelsLikeTemp, double temp, String typeOfWeather, double windSpeed) {
        this.icon = icon;
        this.isItCorrect = isItCorrect;
        this.feelsLikeTemp = feelsLikeTemp;
        this.temp = temp;
        this.typeOfWeather = typeOfWeather;
        this.windSpeed = windSpeed;
    }


    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public double getFeelsLikeTemp() {
        return feelsLikeTemp;
    }

    public void setFeelsLikeTemp(double feelsLikeTemp) {
        this.feelsLikeTemp = feelsLikeTemp;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public String getTypeOfWeather() {
        return typeOfWeather;
    }

    public void setTypeOfWeather(String typeOfWeather) {
        this.typeOfWeather = typeOfWeather;
    }

    public boolean getIsItCorrect() {
        return isItCorrect;
    }

    public void setItCorrect(boolean itCorrect) {
        isItCorrect = itCorrect;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

}
