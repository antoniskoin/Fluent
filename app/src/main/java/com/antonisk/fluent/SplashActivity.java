package com.antonisk.fluent;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SplashActivity extends AppCompatActivity {

    private HashMap<String, String> languages;
    private final String LANGUAGES = "LANGUAGES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Thread langThread = new Thread(() -> {
            try {
                languages = getLanguages();
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(LANGUAGES, languages);
                startActivity(intent);
                finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        langThread.start();
    }

    private HashMap<String, String> getLanguages() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://trans.zillyhuhn.com/languages")
                .build();

        try (Response response = client.newCall(request).execute()) {
            HashMap<String, String> languages = new HashMap<>();
            String jsonData = Objects.requireNonNull(response.body()).string();
            try {
                JSONArray jsonArray = new JSONArray(jsonData);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = new JSONObject(jsonArray.get(i).toString());
                    String name = (String) jsonObject.get("name");
                    String code = (String) jsonObject.get("code");
                    languages.put(name, code);
                }
                return languages;
            } catch (JSONException e) {
                languages.put("error", e.toString());
                return languages;
            }
        }
    }
}