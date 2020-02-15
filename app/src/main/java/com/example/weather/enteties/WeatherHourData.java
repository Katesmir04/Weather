package com.example.weather.enteties;

import android.graphics.Bitmap;

import java.util.Date;


public class WeatherHourData {
    Date time;
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
    int weatherCode;
    int minC;
    int maxC;

    public WeatherHourData() {
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
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

    public int getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(int weatherCode) {
        this.weatherCode = weatherCode;
    }

    public int getMinC() {
        return minC;
    }

    public void setMinC(int minC) {
        this.minC = minC;
    }

    public int getMaxC() {
        return maxC;
    }

    public void setMaxC(int maxC) {
        this.maxC = maxC;
    }
}
