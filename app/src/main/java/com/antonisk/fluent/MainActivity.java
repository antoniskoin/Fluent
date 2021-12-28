package com.antonisk.fluent;

import static helpers.Keyboard.hideKeyboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import helpers.SpeechLocale;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText txtToTranslate, txtTranslated;
    private Spinner spinnerFrom, spinnerTo;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        txtToTranslate = (TextInputEditText) findViewById(R.id.textToTranslate);
        txtTranslated = (TextInputEditText) findViewById(R.id.translatedText);
        Button btnTranslate = findViewById(R.id.btnTranslate);
        Button btnReset = findViewById(R.id.btnReset);
        Button btnCopy = findViewById(R.id.btnCopy);
        Button btnRead = findViewById(R.id.btnRead);

        HashMap<String, String> languages = (HashMap<String, String>) intent.getSerializableExtra("LANGUAGES");
        final List<String> list = new ArrayList<>(languages.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, list);
        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);
        int englishPos = adapter.getPosition("English");
        int germanPos = adapter.getPosition("German");
        spinnerFrom.setSelection(englishPos);
        spinnerTo.setSelection(germanPos);

        btnCopy.setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Translated Text", txtTranslated.getText());
            clipboard.setPrimaryClip(clip);
        });

        textToSpeech = new TextToSpeech(getApplicationContext(), i -> {
            if(i!=TextToSpeech.ERROR){
                textToSpeech.setLanguage(SpeechLocale.DecideLocale(spinnerTo.getSelectedItem().toString()));
            }
        });

        btnRead.setOnClickListener(view -> {
            String text = Objects.requireNonNull(txtTranslated.getText()).toString();
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH,null, null);
        });

        btnTranslate.setOnClickListener(view -> {
            hideKeyboard(this);
            String url = "https://trans.zillyhuhn.com/translate";
            String toTranslate = Objects.requireNonNull(txtToTranslate.getText()).toString();
            String source = (String) spinnerFrom.getSelectedItem();
            String target = (String) spinnerTo.getSelectedItem();
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        String result = post(url, toTranslate, languages.get(source), languages.get(target));
                        runOnUiThread(() -> txtTranslated.setText(result));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        });

        btnReset.setOnClickListener(view -> {
            txtToTranslate.setText("");
            txtTranslated.setText("");
        });
    }

    private String post(String url, String query, String source, String target) throws IOException {
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("q", query)
                .add("source", source)
                .add("target", target)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String jsonData = Objects.requireNonNull(response.body()).string();
            JSONObject jsonObject = new JSONObject(jsonData);
            return jsonObject.getString("translatedText");
        } catch (JSONException e) {
            return "Something went wrong while retrieving translation.";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.itemAbout){
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}

