package com.example.weather;

import android.graphics.Bitmap;

public class WeatherHourData {
    String time;
    int tempC;
    String weatherDesc;
    String urlIcon;
    int chanceOfRain;
    int chanceOfWindy;
    int chanceOfOvercast;
    int chanceOfSunshine;
    int chanceOfFog;
    int chanceOfSnow;
    int chanceOfThunder;
    int humidity;
    int windspeed;
    String winddir;
    String dateUpdate;
    int pressure;
    Bitmap image;

    public WeatherHourData() {
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getTempC() {
        return tempC;
    }

    public void setTempC(int tempC) {
        this.tempC = tempC;
    }

    public String getWeatherDesc() {
        return weatherDesc;
    }

    public void setWeatherDesc(String weatherDesc) {
        this.weatherDesc = weatherDesc;
    }

    public String getUrlIcon() {
        return urlIcon;
    }

    public void setUrlIcon(String urlIcon) {
        this.urlIcon = urlIcon;
    }

    public int getChanceOfRain() {
        return chanceOfRain;
    }

    public void setChanceOfRain(int chanceOfRain) {
        this.chanceOfRain = chanceOfRain;
    }

    public int getChanceOfWindy() {
        return chanceOfWindy;
    }

    public void setChanceOfWindy(int chanceOfWindy) {
        this.chanceOfWindy = chanceOfWindy;
    }

    public int getChanceOfOvercast() {
        return chanceOfOvercast;
    }

    public void setChanceOfOvercast(int chanceOfOvercast) {
        this.chanceOfOvercast = chanceOfOvercast;
    }

    public int getChanceOfSunshine() {
        return chanceOfSunshine;
    }

    public void setChanceOfSunshine(int chanceOfSunshine) {
        this.chanceOfSunshine = chanceOfSunshine;
    }

    public int getChanceOfFog() {
        return chanceOfFog;
    }

    public void setChanceOfFog(int chanceOfFog) {
        this.chanceOfFog = chanceOfFog;
    }

    public int getChanceOfSnow() {
        return chanceOfSnow;
    }

    public void setChanceOfSnow(int chanceOfSnow) {
        this.chanceOfSnow = chanceOfSnow;
    }

    public int getChanceOfThunder() {
        return chanceOfThunder;
    }

    public void setChanceOfThunder(int chanceOfThunder) {
        this.chanceOfThunder = chanceOfThunder;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getWindspeed() {
        return windspeed;
    }

    public void setWindspeed(int windspeed) {
        this.windspeed = windspeed;
    }

    public String getWinddir() {
        return winddir;
    }

    public void setWinddir(String winddir) {
        this.winddir = winddir;
    }

    public String getDateUpdate() {
        return dateUpdate;
    }

    public void setDateUpdate(String dateUpdate) {
        this.dateUpdate = dateUpdate;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
}
