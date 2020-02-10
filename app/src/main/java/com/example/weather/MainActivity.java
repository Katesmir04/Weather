package com.example.weather;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {


    public static final String TAG = "my_log";
    public static final String URL_API = "http://api.worldweatheronline.com/premium/v1/weather.ashx?num_of_days=2&tp=1&format=json&lang=ru&showlocaltime=yes&mca=no";
    public static final String API_KEY = "1b8eca6cb38e4ed48ea71659201801";

    private FusedLocationProviderClient fusedLocationClient;

    private int locationRequestCode = 1000;
    private ArrayList<WeatherHourData> mDataArrayList;
    private LinearLayout mInformer;

    LocationManager locManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if (locManager != null && !locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Log.e(TAG, "GPS IS NOT enabled.");
        } else {
            Log.d(TAG, "GPS is enabled.");
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //Получение последнего местоположения
        getLastLocation();


        //Проверка разрешений от пользователя
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);

        } else {
            // Разрешения есть
            Log.d(TAG, "already permission granted");
        }

        // Обновление координат по клику кнопки
//        Button button = findViewById(R.id.button);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getLastLocation();
//            }
//        });
        mInformer = findViewById(R.id.informer);
    }

    private void createInformer(ArrayList<WeatherHourData> arrayList){

        SimpleDateFormat format = new SimpleDateFormat("H", new Locale("ru"));
        Date dateNow = new Date();
        String dateStr = format.format(dateNow) + ":00";
        mInformer.removeAllViews();
        View leftSide = getLayoutInflater().inflate(R.layout.left_side, null, false);
        TextView vind = leftSide.findViewById(R.id.wind);   // Скорость ветра направление
        TextView davl = leftSide.findViewById(R.id.davl);  //Давление
        TextView vlag = leftSide.findViewById(R.id.vlag);  //Влажность
        // Первый элемент массива с данными о погоде
        WeatherHourData weather = arrayList.get(0);
        vind.setText(weather.getWindspeed() + " м/с, " + weather.getWinddir());
        davl.setText(weather.getPressure() + " мм рт. ст.");
        vlag.setText(weather.getHumidity() + "%");
        //Добавляем данные в информер
        mInformer.addView(leftSide);
        boolean record = false;
        for (WeatherHourData data : arrayList) {
            //Проверяем соответствие времени данных и текущего, чтоб отсеить устаревшие данные
            if(!record && data.getTime().equals(dateStr)) {
                record = true;
                data.setTime("Сейчас");
            }
            View rowSide = getLayoutInflater().inflate(R.layout.row_side, null, false);
            TextView time = rowSide.findViewById(R.id.time);
            ImageView image = rowSide.findViewById(R.id.image);
            TextView temp = rowSide.findViewById(R.id.temp);
            TextView chance = rowSide.findViewById(R.id.chance);
            time.setText(data.getTime());
            image.setImageBitmap(data.getImage());
            temp.setText(data.getTempC() + "°");

            int chanseOfRain = data.getChanceOfRain();  // Вероятность дождя
            int chanseOfSnow = data.getChanceOfSnow();  //Вероятность снега
            int chanseOfThunder = data.getChanceOfThunder();//Вероятность грозы\града

            //Если вероятнось выше 30% то показываем
            if(chanseOfRain > 30){
                chance.setText(chanseOfRain + "%");
            }else if(chanseOfSnow > 30){
                chance.setText(chanseOfSnow + "%");
            }else if(chanseOfThunder > 30){
                chance.setText(chanseOfThunder + "%");
            }else {
                chance.setText("");
            }


            //Добавляем  данные в информер
            if(record)
                mInformer.addView(rowSide);
        }

    }

    //Запрашиваем данные о погоде по координатам
    private void getInfoWeather(double lat, double lng){
        Ion.with(this)
                .load(URL_API + "&key="+API_KEY + "&q=" + lat + "," + lng)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            Log.d(TAG, "Error = " + e);
                        } else {
                            Log.d(TAG, "result = " + result);
                            String dateUpdate = "";
                            JsonArray time_zone = result.get("data").getAsJsonObject().get("time_zone").getAsJsonArray();
                            for (JsonElement j : time_zone) {
                                Log.d(TAG, "time_zone = " + j);
                                dateUpdate = j.getAsJsonObject().get("localtime").getAsString();  // Дата обновления
                            }

                            JsonArray weather = result.get("data").getAsJsonObject().get("weather").getAsJsonArray();  // Массив с данными о  погоде, в каждом элементе 1 день


                            mDataArrayList = new ArrayList<>();  // Список для хранения обектов с данными о погоде
                            for (JsonElement j : weather) {
                                JsonArray astronomy = j.getAsJsonObject().get("astronomy").getAsJsonArray();
                                for (JsonElement astr : astronomy) {
                                    String sunrise = astr.getAsJsonObject().get("sunrise").getAsString();// Время восхода солнца
                                    String sunset = astr.getAsJsonObject().get("sunset").getAsString(); //Время захода солнца

                                }
                                JsonArray hourly = j.getAsJsonObject().get("hourly").getAsJsonArray();  //Массив сданными о погоде по часам
                                //     Log.d(TAG, "hourly = " + hourly);
//                                JsonArray hourly = result.get("hourly").getAsJsonArray();

                                for (JsonElement hour : hourly) {
                                    //  Log.d(TAG, "hourly = " + hour);
                                    int time = hour.getAsJsonObject().get("time").getAsInt(); //Время
                                    int tempC = hour.getAsJsonObject().get("tempC").getAsInt();  //Температура
                                    String weatherDesc = hour.getAsJsonObject().get("lang_ru").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();  //Описание погоды
                                    String urlIcon = hour.getAsJsonObject().get("weatherIconUrl").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();  //Ссылка на иконку
                                    int chanceofrain = hour.getAsJsonObject().get("chanceofrain").getAsInt();  //Вероятнось дождя
                                    int chanceofwindy = hour.getAsJsonObject().get("chanceofwindy").getAsInt();
                                    int chanceofovercast = hour.getAsJsonObject().get("chanceofovercast").getAsInt();
                                    int chanceofsunshine = hour.getAsJsonObject().get("chanceofsunshine").getAsInt();
                                    int chanceoffog = hour.getAsJsonObject().get("chanceoffog").getAsInt();
                                    int chanceofsnow = hour.getAsJsonObject().get("chanceofsnow").getAsInt(); //Вероятнось снега
                                    int chanceofthunder = hour.getAsJsonObject().get("chanceofthunder").getAsInt(); //Вероятнось грозы/града
                                    int humidity =  hour.getAsJsonObject().get("humidity").getAsInt();  //Влажность
                                    int windspeedKmph = hour.getAsJsonObject().get("windspeedKmph").getAsInt(); //Скорость ветра
                                    int pressure = hour.getAsJsonObject().get("pressure").getAsInt();  //Давление
                                    int weatherCode = hour.getAsJsonObject().get("weatherCode").getAsInt();  //Код погоды
                                    String winddir16Point = hour.getAsJsonObject().get("winddir16Point").getAsString();  //Направление ветра в 16 точном компасе
                                    WeatherHourData weatherHourData = new WeatherHourData();  //Объект для хранения данных
                                    weatherHourData.setTime(getTimeString(time));
                                    weatherHourData.setTempC(tempC);

                                    weatherHourData.setWeatherDesc(weatherDesc);
                                    weatherHourData.setUrlIcon(urlIcon);
                                    weatherHourData.setChanceOfRain(chanceofrain);
                                    weatherHourData.setChanceOfWindy(chanceofwindy);
                                    weatherHourData.setChanceOfOvercast(chanceofovercast);
                                    weatherHourData.setChanceOfSunshine(chanceofsunshine);
                                    weatherHourData.setChanceOfFog(chanceoffog);
                                    weatherHourData.setChanceOfSnow(chanceofsnow);
                                    weatherHourData.setChanceOfThunder(chanceofthunder);
                                    weatherHourData.setWindspeed(getMs(windspeedKmph));
                                    weatherHourData.setWinddir(getCompasSide(winddir16Point));
                                    weatherHourData.setHumidity(humidity);
                                    weatherHourData.setDateUpdate(dateUpdate);
                                    weatherHourData.setPressure(getMMrt(pressure));
                                    weatherHourData.setImage(getImageweather(time, weatherCode));
                                    mDataArrayList.add(weatherHourData); //Сохраняем данные в массив


                                }

                            }
                            createInformer(mDataArrayList);  //Переходим к созданию информера

                        }


                    }
                });

    }

    private Date stringToDate(String string){
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("hh:mm A", new Locale("ru"));
        try {
            date = dateFormat.parse(string);
        } catch (ParseException e) {
            Log.d(TAG, "Eroro = " + e);
            e.printStackTrace();
        }
        return date;
    }

    private Bitmap getImageweather(int time, int code){   //Получения картинки погоды по коду
        switch (code){
            case 113:
                if(time >= 0 && time <= 500){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_113n);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_113d);
                }
            case 116:
                if(time >= 0 && time <= 500){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_116n);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_116d);
                }
            case 119:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_122d);
            case 122:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_122d);
            case 143:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_143d);
            case 176:
                if(time >= 0 && time <= 500){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356n);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356d);
                }
            case 179:
                if(time >= 0 && time <= 500){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395n);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395d);
                }
            case 182:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_179d);
            case 185:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_179d);
            case 200:
                if(time >= 0 && time <= 500){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_392n);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_392d);
                }
            case 227:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_320d);
            case 230:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_338d);
            case 260:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_143d);
            case 248:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_143d);
            case 263:
                if(time >= 0 && time <= 500){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356n);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356d);
                }
            case 266:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_359d);
            case 281:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_179d);
            case 284:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_179d);
            case 293:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_359d);
            case 296:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_296d);
            case 299:
                if(time >= 0 && time <= 500){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356n);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356d);
                }
            case 302:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_296d);
            case 305:
                if(time >= 0 && time <= 500){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356n);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356d);
                }
            case 308:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_296d);
            case 311:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_179d);
            case 314:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_179d);
            case 317:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_179d);
            case 320:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_320d);
            case 323:
                if(time >= 0 && time <= 500){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395n);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395d);
                }
            case 326:
                if(time >= 0 && time <= 500){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395n);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395d);
                }
            case 329:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_338d);
            case 332:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_338d);
            case 335:
                if(time >= 0 && time <= 500){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395n);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395d);
                }
            case 338:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_338d);
            case 350:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_179d);
            case 353:
                if(time >= 0 && time <= 500){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356n);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356d);
                }
            case 356:
                if(time >= 0 && time <= 500){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356n);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356d);
                }
            case 359:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_296d);
            case 362:
                if(time >= 0 && time <= 500){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395n);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395d);
                }
            case 365:
                if(time >= 0 && time <= 500){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395n);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395d);
                }
            case 368:
                if(time >= 0 && time <= 500){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395n);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395d);
                }
            case 371:
                if(time >= 0 && time <= 500){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395n);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395d);
                }
            case 374:
                if(time >= 0 && time <= 500){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395n);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395d);
                }
            case 377:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_377d);
            case 386:
                if(time >= 0 && time <= 500){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_392n);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_392d);
                }
            case 389:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_389d);
            case 392:
                if(time >= 0 && time <= 500){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_392n);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_392d);
                }
            case 395:
                if(time >= 0 && time <= 500){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395n);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395d);
                }
            default:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_no);
        }
    }

    //Перевод милибаров а мм рт ст
    private int getMMrt(int pressure){
        return (int)(pressure * 0.75);
    }

    //Перевод км\ч в м\с
    private int getMs(int kmCh){
        return (kmCh * 1000)/3600;
    }

    //Приведение времени в нужный формат 0 -> 00:00, 100 -> 01:00
    private String getTimeString(int time){
        switch (time){
            case 0:
                return "00:00";
            case 100:
                return "01:00";
            case 200:
                return "02:00";
            case 300:
                return "03:00";
            case 400:
                return "04:00";
            case 500:
                return "05:00";
            case 600:
                return "06:00";
            case 700:
                return "07:00";
            case 800:
                return "08:00";
            case 900:
                return "09:00";
            case 1000:
                return "10:00";
            case 1100:
                return "11:00";
            case 1200:
                return "12:00";
            case 1300:
                return "13:00";
            case 1400:
                return "14:00";
            case 1500:
                return "15:00";
            case 1600:
                return "16:00";
            case 1700:
                return "17:00";
            case 1800:
                return "18:00";
            case 1900:
                return "19:00";
            case 2000:
                return "20:00";
            case 2100:
                return "21:00";
            case 2200:
                return "22:00";
            case 2300:
                return "23:00";
            default:
                return "00:00";
        }
    }

    //Преобразование данных направления ветра
    private String getCompasSide(String compas){
        switch (compas){
            case "N":
                return "С";
            case "NNE":
                return "ССВ";
            case "NE":
                return "СВ";
            case "ENE":
                return "ВСВ";
            case "E":
                return "В";
            case "ESE":
                return "ВЮВ";
            case "SE":
                return "ЮВ";
            case "SSE":
                return "ЮЮВ";
            case "S":
                return "Ю";
            case "SSW":
                return "ЮЮЗ";
            case "SW":
                return "ЮЗ";
            case "WSW":
                return "ЗЮЗ";
            case "W":
                return "З";
            case "WNW":
                return "ЗСЗ";
            case "NW":
                return "СЗ";
            case "NNW":
                return "ССЗ";
            default:
                return "";
        }


    }

    //Получения последнего местоположения
    private void getLastLocation(){
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            getInfoWeather(location.getLatitude(), location.getLongitude());  //Запрос погоды по координатам
                        }else{
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_location), Snackbar.LENGTH_SHORT).show();  //Выводим сообщение об ошибке
                        }
                    }
                });
    }

    //Вызывается при получении разрешения от пользователя на доступ к геолокации
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Получения последнего местоположения
                    getLastLocation();
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.premission_dained), Snackbar.LENGTH_INDEFINITE).setAction(R.string.enable, new EnableLocation()).show();
                    //Сообщение пользователю что он не дал разрешение, с возможностью повторить запрос разрешения
                }
                break;
            }
        }
    }

    //Обработка нажатия кнопки в Snackbar
    public class EnableLocation implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


}


