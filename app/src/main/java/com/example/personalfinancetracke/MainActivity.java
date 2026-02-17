package com.example.personalfinancetracke;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends Activity {

    private TextView tvResult;
    private Button btnLoad;

    private static final String API_URL =
            "https://dummyjson.com/users/1";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        tvResult = findViewById(R.id.tvResult);
        btnLoad = findViewById(R.id.btnLoad);

        btnLoad.setOnClickListener(v -> loadDataFromInternet());
    }

    // 3-қадам: HTTP арқылы дерек жүктеу (фонда)
    private void loadDataFromInternet() {
        tvResult.setText("Жүктелуде...");

        new Thread(() -> {
            try {
                URL url = new URL(API_URL);

                HttpsURLConnection connection =
                        (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpsURLConnection.HTTP_OK) {
                    throw new Exception("HTTP Error: " + responseCode);
                }

                // InputStream оқу -> JSON String жинау
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );

                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();
                connection.disconnect();

                String json = builder.toString();

                // 4-қадам: Gson арқылы десериализация
                User user = new Gson().fromJson(json, User.class);

                // 5-қадам: UI-де көрсету (негізгі ағында)
                runOnUiThread(() -> {
                    String result =
                            "ID: " + user.getId() + "\n" +
                                    "Name: " + user.getName() + "\n" +
                                    "Email: " + user.getEmail() + "\n" +
                                    "Phone: " + user.getPhone();
                    tvResult.setText(result);
                });

            } catch (Exception e) {
                runOnUiThread(() -> tvResult.setText("Қате: " + e.getMessage()));
            }
        }).start();
    }
}
