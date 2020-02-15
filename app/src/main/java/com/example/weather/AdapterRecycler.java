package com.example.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.enteties.RecomendObject;

import java.util.ArrayList;



public class AdapterRecycler extends RecyclerView.Adapter<AdapterRecycler.ViewHolder> {

    ArrayList<RecomendObject> mArrayList;
    Context mContext;

    public AdapterRecycler(ArrayList<RecomendObject> array, Context context) {
        mArrayList = array;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);
        return new ViewHolder(v);
    }



    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final RecomendObject object = mArrayList.get(position);
        holder.text.setText(object.getName());
        //Преобразование и загрузка данных картинки(формат только svg)
        //Utils.fetchSvg(mContext, object.getUrlImage(), holder.image);


    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView text;
        ImageView image;

        public ViewHolder(@NonNull View v) {
            super(v);
            text = v.findViewById(R.id.text);
            image = v.findViewById(R.id.image);
        }
    }

}
