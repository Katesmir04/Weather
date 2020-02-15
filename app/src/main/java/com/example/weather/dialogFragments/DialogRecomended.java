package com.example.weather.dialogFragments;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.AdapterRecycler;
import com.example.weather.R;
import com.example.weather.enteties.RecomendObject;

import java.util.ArrayList;




public class DialogRecomended extends DialogFragment {


    ArrayList<RecomendObject> mRecomendObjectArrayList;
    RecyclerView mRecyclerView;
    AdapterRecycler mAdapter;

    public DialogRecomended(ArrayList<RecomendObject> arrayList) {
        mRecomendObjectArrayList = arrayList;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_recomended, container, false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));  //прозрачность фона
        mRecyclerView = view.findViewById(R.id.recycler);
        mAdapter = new AdapterRecycler(mRecomendObjectArrayList, getContext());  //Адаптер для работы с данными
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

}
