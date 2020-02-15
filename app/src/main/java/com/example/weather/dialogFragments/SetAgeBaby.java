package com.example.weather.dialogFragments;


import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.weather.MainActivity;
import com.example.weather.R;
import com.example.weather.enteties.RecomendObject;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.example.weather.MainActivity.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class SetAgeBaby extends DialogFragment {

    public static final String TEMPER = "TEMPER";
    public static final String AGE = "AGE";
    public static final String SELECT_DATE_TIME = "SELECT_DATE";

    public SetAgeBaby() {
        // Required empty public constructor
    }
    private int temp = 0;
    FrameLayout mLoading;
    SharedPreferences mPreferences;
    Date selectDate;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        if(getArguments() != null)
            temp = getArguments().getInt(TEMPER,0);
        View view = inflater.inflate(R.layout.fragment_set_age_baby, container, false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if(mPreferences.contains(SELECT_DATE_TIME))  //Получени есохраненной даты рождения
            selectDate = new Date(mPreferences.getLong(SELECT_DATE_TIME, 0));
        else
            selectDate = new Date();
        mLoading = view.findViewById(R.id.loading);
        mLoading.setVisibility(View.GONE);
        final DatePicker calendarView = view.findViewById(R.id.calendarView);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectDate);  //Устанавливаем сохраненную дату в календарь

        calendarView.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE)); //Устанавливаем сохраненную дату в календарь
        calendarView.setMaxDate(new Date().getTime());  //Максимальная дата, текущая

        Button save = view.findViewById(R.id.button);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = calendarView.getYear();
                int month = calendarView.getMonth();
                int dayOfMonth = calendarView.getDayOfMonth();
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                Date selectDate = calendar.getTime();
                setSharedPref(SELECT_DATE_TIME, selectDate.getTime());  // Сохраняем дату
                Date dateNow = new Date();
                long hour = (((dateNow.getTime() - selectDate.getTime()) / (1000 * 60 * 60 )));  // Разница с текущей датой в часах
                long ageMonth = (hour / 24) / 30; // Разница с текущей датой в месяцах
                float age = (ageMonth * 0.0833334f);  // Возраст
                setSharedPref(AGE, age);  // Сохраняем возраст
                getInfoFromServer(temp, age); //Запрашиваем данные с сервера

            }
        });


        return view;
    }


    //Сохранение данных в SharedPreferences
    private void setSharedPref(String name,float value){
        SharedPreferences.Editor ed = mPreferences.edit();
        ed.putFloat(name, value);
        ed.apply();
    }

    //Сохранение данных в SharedPreferences
    private void setSharedPref(String name,long value){
        SharedPreferences.Editor ed = mPreferences.edit();
        ed.putLong(name, value);
        ed.apply();
    }

    //Запрос данных в веб сервисе
    private void getInfoFromServer(int temp, double age){
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
                            if(getActivity() != null)
                                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.error_load), Snackbar.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "Result = " + result);
                            if(result.get("message").getAsBoolean()) {
                                JsonArray arr = result.get("result").getAsJsonArray();
                                ArrayList<RecomendObject> arrayList = new ArrayList<>();
                                for (JsonElement j : arr) {
                                    String name = j.getAsJsonObject().get("name").getAsString();
                                    String url = j.getAsJsonObject().get("image").getAsString();
                                    RecomendObject recomendObject = new RecomendObject(name, url);
                                    arrayList.add(recomendObject);
                                }
                                if(((MainActivity)getActivity()) != null)
                                    //Передаем данные в активность
                                       ((MainActivity)getActivity()).setmRecomendObject(arrayList);
                                // Закрываем диалог
                                    dismissAllowingStateLoss();
                            }
                        }
                        mLoading.setVisibility(View.GONE);
                    }
                });
    }

}
