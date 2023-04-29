package main.java.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import main.java.adapter.LocalDateTimeAdapter;

import java.time.LocalDateTime;

public class GsonManager {

    public static Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeNulls();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        return gsonBuilder.create();
    }


}