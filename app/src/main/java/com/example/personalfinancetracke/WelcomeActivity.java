package com.example.personalfinancetracke;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "FinanceTrackerPrefs";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_CURRENCY = "currency";
    private static final String KEY_FIRST_LAUNCH = "first_launch";

    private EditText nameEditText;  // ← ИЗМЕНЕНО: было TextInputEditText
    private Spinner currencySpinner;
    private Button continueButton;

    private String[] currencies = {"₸ Tenge (KZT)", "$ Dollar (USD)", "€ Euro (EUR)", "₽ Ruble (RUB)"};
    private String[] currencySymbols = {"₸", "$", "€", "₽"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Проверяем, первый ли это запуск
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true);

        // Если не первый запуск - сразу переходим в MainActivity
        if (!isFirstLaunch) {
            startMainActivity();
            return;
        }

        setContentView(R.layout.activity_welcome);

        nameEditText = findViewById(R.id.name_edit_text);
        currencySpinner = findViewById(R.id.currency_spinner);
        continueButton = findViewById(R.id.continue_button);

        // Настройка Spinner с валютами
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                currencies
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(adapter);

        // Устанавливаем Tenge по умолчанию
        currencySpinner.setSelection(0);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
    }

    /**
     * Сохраняет настройки и переходит в главное приложение
     */
    private void saveSettings() {
        String name = nameEditText.getText().toString().trim();

        // Валидация имени
        if (name.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_name, Toast.LENGTH_SHORT).show();
            nameEditText.requestFocus();
            return;
        }

        // Получаем выбранную валюту
        int currencyIndex = currencySpinner.getSelectedItemPosition();
        String currencySymbol = currencySymbols[currencyIndex];

        // Сохраняем в SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_CURRENCY, currencySymbol);
        editor.putBoolean(KEY_FIRST_LAUNCH, false);
        editor.apply();

        // Переходим в MainActivity
        startMainActivity();
    }

    /**
     * Запускает MainActivity и закрывает WelcomeActivity
     */
    private void startMainActivity() {
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Закрываем WelcomeActivity, чтобы нельзя было вернуться
    }

    @Override
    public void onBackPressed() {
        // Отключаем возврат назад на экране приветствия
        // Пользователь должен заполнить данные
    }
}