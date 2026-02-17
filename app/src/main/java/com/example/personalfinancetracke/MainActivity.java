package com.example.personalfinancetracke;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int ADD_OPERATION_REQUEST = 1;
    private static final String PREFS_NAME = "FinanceTrackerPrefs";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_CURRENCY = "currency";

    private DatabaseHelper dbHelper;
    private TextView balanceTextView;
    private TextView incomeTextView;
    private TextView expenseTextView;
    private TextView statusTextView;
    private ProgressBar progressBar;
    private ListView operationsListView;
    private FloatingActionButton fabAdd;
    private OperationAdapter adapter;
    private List<Operation> operations;

    private String userName = "";
    private String currencySymbol = "₸";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Загружаем настройки пользователя
        loadUserSettings();

        // Устанавливаем заголовок с именем пользователя
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.welcome_user, userName));
        }

        dbHelper = new DatabaseHelper(this);

        balanceTextView = findViewById(R.id.balance_text_view);
        incomeTextView = findViewById(R.id.income_text_view);
        expenseTextView = findViewById(R.id.expense_text_view);
        statusTextView = findViewById(R.id.status_text_view);
        progressBar = findViewById(R.id.progress_bar);
        operationsListView = findViewById(R.id.operations_list_view);
        fabAdd = findViewById(R.id.fab_add);

        operations = new ArrayList<>();
        adapter = new OperationAdapter(this, operations, currencySymbol);
        operationsListView.setAdapter(adapter);

        // Long click to delete
        operationsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Operation operation = operations.get(position);
                dbHelper.deleteOperation(operation.getId());
                loadOperations();
                return true;
            }
        });

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddOperationActivity.class);
                startActivityForResult(intent, ADD_OPERATION_REQUEST);
            }
        });

        // Загружаем операции асинхронно
        loadOperations();
    }

    /**
     * Загружает настройки пользователя из SharedPreferences
     */
    private void loadUserSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userName = prefs.getString(KEY_USER_NAME, "User");
        currencySymbol = prefs.getString(KEY_CURRENCY, "₸");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Добавляем меню для сброса настроек
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_reset_settings) {
            resetSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Сбрасывает настройки и возвращает на экран приветств��я
     */
    private void resetSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("first_launch", true);
        editor.apply();

        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_OPERATION_REQUEST && resultCode == RESULT_OK) {
            loadOperations();
        }
    }

    /**
     * Запускает асинхронную загрузку операций из БД
     */
    private void loadOperations() {
        new LoadOperationsTask().execute();
    }

    /**
     * Обновляет UI с загруженными данными
     */
    private void updateUI(List<Operation> loadedOperations) {
        operations.clear();
        operations.addAll(loadedOperations);
        adapter.notifyDataSetChanged();
        updateBalance();
    }

    /**
     * Пересчитывает и обновляет баланс с выбранной валютой
     */
    private void updateBalance() {
        double income = 0;
        double expense = 0;

        for (Operation op : operations) {
            if (op.getType().equals("income")) {
                income += op.getAmount();
            } else {
                expense += op.getAmount();
            }
        }

        double balance = income - expense;

        balanceTextView.setText(String.format("%s%.2f", currencySymbol, balance));
        incomeTextView.setText(String.format("↑ %.2f", income));
        expenseTextView.setText(String.format("↓ %.2f", expense));
    }

    /**
     * AsyncTask для загрузки операций из SQLite в фоновом потоке
     */
    private class LoadOperationsTask extends AsyncTask<Void, Integer, List<Operation>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            statusTextView.setText(R.string.status_loading);
            fabAdd.setEnabled(false);
            fabAdd.setAlpha(0.5f);
        }

        @Override
        protected List<Operation> doInBackground(Void... voids) {
            publishProgress(10);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            publishProgress(50);
            List<Operation> loadedOperations = dbHelper.getAllOperations();

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            publishProgress(100);

            return loadedOperations;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            if (values.length > 0) {
                int progress = values[0];
                progressBar.setProgress(progress);

                if (progress < 50) {
                    statusTextView.setText(R.string.status_loading);
                } else if (progress < 100) {
                    statusTextView.setText(R.string.status_processing);
                }
            }
        }

        @Override
        protected void onPostExecute(List<Operation> loadedOperations) {
            super.onPostExecute(loadedOperations);
            updateUI(loadedOperations);
            progressBar.setVisibility(View.GONE);
            statusTextView.setText(R.string.status_ready);
            fabAdd.setEnabled(true);
            fabAdd.setAlpha(1.0f);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            progressBar.setVisibility(View.GONE);
            statusTextView.setText(R.string.status_cancelled);
            fabAdd.setEnabled(true);
            fabAdd.setAlpha(1.0f);
        }
    }
}