package com.example.weather.enteties;


public class RecomendObject {
    String name;
    String urlImage;

    public RecomendObject(String name, String urlImage) {
        this.name = name;
        this.urlImage = urlImage;
    }

    public String getName() {
        return name;
    }

    public String getUrlImage() {
        return urlImage;
    }
}
