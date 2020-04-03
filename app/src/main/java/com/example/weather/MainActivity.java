package com.example.weather;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;

import com.example.weather.dialogFragments.DialogRecomended;
import com.example.weather.dialogFragments.SetAgeBaby;
import com.example.weather.enteties.RecomendObject;
import com.example.weather.enteties.WeatherHourData;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.weather.dialogFragments.SetAgeBaby.AGE;
import static com.example.weather.dialogFragments.SetAgeBaby.TEMPER;

public class MainActivity extends AppCompatActivity {


    public static final String TAG = "my_log";
    public static final String URL_API = "http://api.worldweatheronline.com/premium/v1/weather.ashx?num_of_days=2&tp=1&format=json&lang=ru&showlocaltime=yes&mca=no";
    public static final String API_KEY = "13d8aa09aeca44af906125733200304";

    private FusedLocationProviderClient fusedLocationClient;

    private int locationRequestCode = 1000;
    private ArrayList<WeatherHourData> mDataArrayList;
    private LinearLayout mInformer;


    enum Typeday {morning, day, evening, night};

    CoordinatorLayout mContainer;

    LocationManager locManager;
    Toolbar mToolbar;
    FrameLayout mLoading;
    TextView mCityName, mDiscrWeather, mTemp, mInfoDay, mInfoTemp, mInfoDayNext, mInfoTempNext;
    String discrWeather;
    int tempC;
    ImageView imageBaby;
    ArrayList<RecomendObject> mRecomendObjectArrayList;

    SharedPreferences mPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mContainer = findViewById(R.id.conteiner);
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.app_name);
        setSupportActionBar(mToolbar);

        mLoading = findViewById(R.id.loading);
        mCityName = findViewById(R.id.cityName);
        mDiscrWeather = findViewById(R.id.discr);
        mTemp = findViewById(R.id.temper);
        mInfoDay = findViewById(R.id.infoText);
        mInfoTemp = findViewById(R.id.infoTemp);

        mInfoDayNext = findViewById(R.id.infoTextNext);
        mInfoTempNext = findViewById(R.id.infoTempNext);


        imageBaby = findViewById(R.id.baby);

        mLoading.setVisibility(View.VISIBLE);

        locManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if (locManager != null && !locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){  // Проверка включения GPS
            Log.e(TAG, "GPS IS NOT enabled.");
        } else {
            Log.d(TAG, "GPS is enabled.");
        }

        // Провайдер местоположения
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //Получение последнего местоположения
        getLastLocation();

        //Проверка наличия разрешений от пользователя, если их нет запрашиваем

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);

        } else {
            // Разрешения есть
            Log.d(TAG, "already permission granted");
        }

        mInformer = findViewById(R.id.informer);

        // Выдвижная панель
        LinearLayout hiddenLay = findViewById(R.id.hidden_lay);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(hiddenLay);
        bottomSheetBehavior.setPeekHeight(50); //Высота выступающей части
        bottomSheetBehavior.setHideable(true);  //Скрытие свайпом
        //  Открытие диалога с рекомендациями
        imageBaby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogRecomended dialogRecomended = new DialogRecomended(mRecomendObjectArrayList);
                dialogRecomended.show(getSupportFragmentManager(), "DialogRecomended");

            }
        });

    }

    public void setmRecomendObject(ArrayList<RecomendObject> arrayList){
        // Массив с объектами рекомендаций
        mRecomendObjectArrayList = arrayList;
    }

    // Создание меню
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        // Клик по кнопке настроек
        if(item.getItemId() == R.id.age){
            showDialogAge();
            //Клик по кнопке обновления
        }else if(item.getItemId() == R.id.refresh){
            getLastLocation();
        }
        return true;

    }

    private void showDialogAge() {
        SetAgeBaby setAgeBaby = new SetAgeBaby();
        Bundle bundle = new Bundle();
        bundle.putInt(TEMPER, tempC);
        setAgeBaby.setArguments(bundle);
        setAgeBaby.show(getSupportFragmentManager(), "SetAgeBaby");
    }

    private void createInformer(ArrayList<WeatherHourData> arrayList) {

        SimpleDateFormat format = new SimpleDateFormat("HH:mm", new Locale("ru"));
        String dateInfoStr;
        Date dateNow = new Date();
        DateFormat format2 = new SimpleDateFormat("EEEE", new Locale("ru"));

        if (arrayList.size() > 0){

            WeatherHourData dataNowDay = arrayList.get(0);  //Данные первого дня
            String dayOfWeek = format2.format(dataNowDay.getTime());  //Получаем день недели
            dayOfWeek = firstUpperCase(dayOfWeek);                    //Делаем первую букву заглавной
            mInfoTemp.setText(String.format(new Locale("ru"), "%d° %d°", dataNowDay.getMaxC(), dataNowDay.getMinC()));
            mInfoDay.setText(String.format("%s Сегодня", dayOfWeek));

            WeatherHourData dataNextDay = arrayList.get(arrayList.size() - 1); //Данные второго дня
            String dayOfWeekNext = format2.format(dataNextDay.getTime());
            dayOfWeekNext = firstUpperCase(dayOfWeekNext);
            mInfoTempNext.setText(String.format(new Locale("ru"), "%d° %d°", dataNextDay.getMaxC(), dataNextDay.getMinC()));
            mInfoDayNext.setText(dayOfWeekNext);

        }


        mInformer.removeAllViews();  // Удаляем все view с нашего ScrollView

        TextView vind = findViewById(R.id.wind);   // Скорость ветра направление
        TextView davl = findViewById(R.id.davl);  //Давление
        TextView vlag = findViewById(R.id.vlag);  //Влажность
        WeatherHourData weather = arrayList.get(0);       // Первый элемент массива с данными о погоде
        vind.setText(String.format(new Locale("ru"),"%d м/с, %s", weather.getWindspeed(), weather.getWinddir()));
        davl.setText(String.format(new Locale("ru"),"%d мм рт. ст.", weather.getPressure()));
        vlag.setText(String.format(new Locale("ru"),"%d%%", weather.getHumidity()));

        boolean record = false;
        for (WeatherHourData data : arrayList) {   //Проходим по массиву с данными
            if(!record && data.getTime().after(dateNow)) {  //Проверяем соответствие времени данных и текущего, чтоб отсеить устаревшие данные
                record = true;
                dateInfoStr  = "Сейчас";
                //setBackground(data.getTempC(), data.getWeatherCode());

                mTemp.setText(String.format("%d°", tempC));
                mDiscrWeather.setText(discrWeather);



            }else{
                dateInfoStr = format.format(data.getTime());
            }
            View rowSide = getLayoutInflater().inflate(R.layout.row_side, null, false);  //Часть информера с данными одного часа
            TextView time = rowSide.findViewById(R.id.time);
            ImageView image = rowSide.findViewById(R.id.image);
            TextView temp = rowSide.findViewById(R.id.temp);
            TextView chance = rowSide.findViewById(R.id.chance);
            time.setText(dateInfoStr);
            image.setImageBitmap(data.getImage());
            temp.setText(String.format(new Locale("ru"),"%d°", data.getTempC()));

            int chanseOfRain = data.getChanceOfRain();  // Вероятность дождя
            int chanseOfSnow = data.getChanceOfSnow();  //Вероятность снега
            int chanseOfThunder = data.getChanceOfThunder();//Вероятность грозы\града

            if(chanseOfRain > 30){                    //Если вероятнось выше 30% то показываем
                chance.setText(String.format(new Locale("ru"),"%d%%", chanseOfRain));
            }else if(chanseOfSnow > 30){
                chance.setText(String.format(new Locale("ru"),"%d%%", chanseOfSnow));
            }else if(chanseOfThunder > 30){
                chance.setText(String.format(new Locale("ru"),"%d%%", chanseOfThunder));
            }else {
                chance.setText("");
            }


            if(record)
                mInformer.addView(rowSide);  //Добавляем  данные в информер

        }


        if(!mPreferences.contains(AGE)){
            mLoading.setVisibility(View.GONE);
            showDialogAge();  //Показываем диалог ввода даты рождения(если еще не вводился)
        }else {
            getInfoFromServer(tempC, mPreferences.getFloat(AGE, 0));  //Если вводился получаем данные с сервера
        }


    }

    private String firstUpperCase(String word){
        if(word == null || word.isEmpty()) return word;
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

   
    //Возращает время суток
    private Typeday getTypeDayFromTime(){
        Calendar calendar = Calendar.getInstance();
        int hour = 0;
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        Typeday result = Typeday.day;
        if(hour > 5 && hour < 12){
            result = Typeday.morning;
        }else if(hour >= 12 && hour <= 18){
            result = Typeday.day;
        }else if(hour > 18 && hour <= 23){
            result = Typeday.evening;
        }else {
            result = Typeday.night;
        }

        return result;
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
                            mLoading.setVisibility(View.GONE);
                        } else {
                            Log.d(TAG, "result = " + result);
                            String dateUpdate = "";
                            JsonArray time_zone = result.get("data").getAsJsonObject().get("time_zone").getAsJsonArray();
                            for (JsonElement j : time_zone) {
                                Log.d(TAG, "time_zone = " + j);
                                dateUpdate = j.getAsJsonObject().get("localtime").getAsString();  // Дата обновления
                            }


                            JsonArray current_condition = result.get("data").getAsJsonObject().get("current_condition").getAsJsonArray();
                            for (JsonElement j : current_condition) {
                                Log.d(TAG, "current_condition = " + j);
                                tempC = j.getAsJsonObject().get("temp_C").getAsInt();
                                discrWeather = j.getAsJsonObject().get("lang_ru").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();

                            }

                            JsonArray weather = result.get("data").getAsJsonObject().get("weather").getAsJsonArray();  // Массив с данными о  погоде, в каждом элементе 1 день


                            mDataArrayList = new ArrayList<>();  // Список для хранения обектов с данными о погоде

                            for (JsonElement j : weather) {
                                String sunrise = "";
                                String sunset = "";
                                String dateDay = j.getAsJsonObject().get("date").getAsString();
                                JsonArray astronomy = j.getAsJsonObject().get("astronomy").getAsJsonArray();
                                for (JsonElement astr : astronomy) {
                                    sunrise = dateDay + " " + astr.getAsJsonObject().get("sunrise").getAsString();// Время восхода солнца
                                    sunset = dateDay + " " + astr.getAsJsonObject().get("sunset").getAsString(); //Время захода солнца

                                }
                                JsonArray hourly = j.getAsJsonObject().get("hourly").getAsJsonArray();  //Массив сданными о погоде по часам
                                int minC = j.getAsJsonObject().get("mintempC").getAsInt();
                                int maxC = j.getAsJsonObject().get("maxtempC").getAsInt();


                                for (JsonElement hour : hourly) {

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

                                    weatherHourData.setTempC(tempC);
                                    weatherHourData.setWeatherCode(weatherCode);
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
                                    Date dateInfo = stringToDateTime(getTimeString(dateDay, time));
                                    weatherHourData.setTime(dateInfo);
                                    weatherHourData.setMinC(minC);
                                    weatherHourData.setMaxC(maxC);
                                    //Log.d(TAG, "dateInfo = " + dateInfo);

                                    weatherHourData.setImage(getImageweather(dateInfo, weatherCode, stringToDate(sunrise), stringToDate(sunset)));
                                    mDataArrayList.add(weatherHourData); //Сохраняем данные в массив


                                }

                            }
                            //Переходим к созданию информера
                            createInformer(mDataArrayList);  //Переходим к созданию информера

                        }


                    }
                });

    }

    private void getInfoFromServer(int temp, double age){  //Запрос данных в веб сервисе
        mLoading.setVisibility(View.VISIBLE);
        JsonObject json = new JsonObject();
        json.addProperty("getInfo", true);
        json.addProperty("temp", temp);  // температура
        json.addProperty("age", age);   // возраст

        Ion.with(this)
                .load("http://androiddev.xyz/weatherApp/getInfo.php")
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            Log.d(TAG, "Error = " + e);
                            //updateInfoBaby("");
                        } else {
                            Log.d(TAG, "Result = " + result);
                            if(result.get("message").getAsBoolean()) {
                                JsonArray arr = result.get("result").getAsJsonArray();
                                ArrayList<RecomendObject> arrayList = new ArrayList<>();
                                for (JsonElement j : arr) {
                                    String name = j.getAsJsonObject().get("name").getAsString(); // Название
                                    String url = j.getAsJsonObject().get("image").getAsString();  // url картинки
                                    RecomendObject recomendObject = new RecomendObject(name, url);
                                    arrayList.add(recomendObject);
                                }

                                setmRecomendObject(arrayList);
                            }
                        }
                        mLoading.setVisibility(View.GONE);
                    }
                });
    }

    private Date stringToDate(String string){  //Преобразование строки в дату

        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US);
        try {
            date = dateFormat.parse(string);
        } catch (ParseException e) {
            Log.d(TAG, "Error = " + e);
            e.printStackTrace();
        }

        return date;
    }

    private Date stringToDateTime(String string){ //Преобразование строки в дату

        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", new Locale("ru"));
        try {
            date = dateFormat.parse(string);
        } catch (ParseException e) {
            Log.d(TAG, "Error = " + e);
            e.printStackTrace();
        }

        return date;
    }

    //Получение картинки погоды по коду и времени суток
    private Bitmap getImageweather(Date timeDay, int code, Date sunrise, Date sunset){
        switch (code){
            case 113:
                if(timeDay.after(sunset) && timeDay.before(sunset)){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_113d);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_113n);
                }
            case 116:
                if(timeDay.after(sunrise) && timeDay.before(sunset)){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_116d);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_116n);
                }
            case 119:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_122d);
            case 122:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_122d);
            case 143:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_143d);
            case 176:
                if(timeDay.after(sunrise) && timeDay.before(sunset)){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356d);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356n);
                }
            case 179:
                if(timeDay.after(sunrise) && timeDay.before(sunset)){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395d);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395n);
                }
            case 182:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_179d);
            case 185:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_179d);
            case 200:
                if(timeDay.after(sunrise) && timeDay.before(sunset)){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_392d);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_392n);
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
                if(timeDay.after(sunrise) && timeDay.before(sunset)){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356d);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356n);
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
                if(timeDay.after(sunrise) && timeDay.before(sunset)){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356d);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356n);
                }
            case 302:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_296d);
            case 305:
                if(timeDay.after(sunrise) && timeDay.before(sunset)){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356d);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356n);
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
                if(timeDay.after(sunrise) && timeDay.before(sunset)){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395d);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395n);
                }
            case 326:
                if(timeDay.after(sunrise) && timeDay.before(sunset)){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395d);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395n);
                }
            case 329:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_338d);
            case 332:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_338d);
            case 335:
                if(timeDay.after(sunrise) && timeDay.before(sunset)){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395d);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395n);
                }
            case 338:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_338d);
            case 350:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_179d);
            case 353:
                if(timeDay.after(sunrise) && timeDay.before(sunset)){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356d);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356n);
                }
            case 356:
                if(timeDay.after(sunrise) && timeDay.before(sunset)){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356d);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_356n);
                }
            case 359:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_359d);
            case 362:
                if(timeDay.after(sunrise) && timeDay.before(sunset)){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395d);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395n);
                }
            case 365:
                if(timeDay.after(sunrise) && timeDay.before(sunset)){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395d);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395n);
                }
            case 368:
                if(timeDay.after(sunrise) && timeDay.before(sunset)){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395d);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395n);
                }
            case 371:
                if(timeDay.after(sunrise) && timeDay.before(sunset)){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395d);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395n);
                }
            case 374:
                if(timeDay.after(sunrise) && timeDay.before(sunset)){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395d);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395n);
                }
            case 377:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_377d);
            case 386:
                if(timeDay.after(sunrise) && timeDay.before(sunset)){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_392d);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_392n);
                }
            case 389:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_389d);
            case 392:
                if(timeDay.after(sunrise) && timeDay.before(sunset)){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_392d);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_392n);
                }
            case 395:
                if(timeDay.after(sunrise) && timeDay.before(sunset)){
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395d);
                }else{
                    return BitmapFactory.decodeResource(getResources(), R.drawable.ic_395n);
                }
            default:
                return BitmapFactory.decodeResource(getResources(), R.drawable.ic_nan);
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
    private String getTimeString(String date, int time){
        switch (time){
            case 0:
                return date + " 00:00";
            case 100:
                return date + " 01:00";
            case 200:
                return date + " 02:00";
            case 300:
                return date + " 03:00";
            case 400:
                return date + " 04:00";
            case 500:
                return date + " 05:00";
            case 600:
                return date + " 06:00";
            case 700:
                return date + " 07:00";
            case 800:
                return date + " 08:00";
            case 900:
                return date + " 09:00";
            case 1000:
                return date + " 10:00";
            case 1100:
                return date + " 11:00";
            case 1200:
                return date + " 12:00";
            case 1300:
                return date + " 13:00";
            case 1400:
                return date + " 14:00";
            case 1500:
                return date + " 15:00";
            case 1600:
                return date + " 16:00";
            case 1700:
                return date + " 17:00";
            case 1800:
                return date + " 18:00";
            case 1900:
                return date + " 19:00";
            case 2000:
                return date + " 20:00";
            case 2100:
                return date + " 21:00";
            case 2200:
                return date + " 22:00";
            case 2300:
                return date + " 23:00";
            default:
                return date + " 00:00";
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
                            Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());  // Запрос адреса по координатам
                            List<Address> addresses = null;
                            try {
                                addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (addresses != null && addresses.size() > 0) {
                                if(addresses.get(0).getLocality() != null)
                                    mCityName.setText(addresses.get(0).getLocality());
                                else
                                    mCityName.setText(addresses.get(0).getFeatureName());
                            }
                            else {
                                mCityName.setText("Не определено");
                            }
                        }else{
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_location), Snackbar.LENGTH_SHORT).show();  //Выводим сообщение об ошибке
                        }
                    }
                });
    }

    //Вызывается при получении разрешения от пользователя на доступ к геолокации
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { //Вызывается при получении разрешения от пользователя на доступ к геолокации
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation(); //Получения последнего место положения
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


