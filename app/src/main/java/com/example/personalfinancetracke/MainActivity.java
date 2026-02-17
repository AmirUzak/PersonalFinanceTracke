package com.example.personalfinancetracke;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        balanceTextView = findViewById(R.id.balance_text_view);
        incomeTextView = findViewById(R.id.income_text_view);
        expenseTextView = findViewById(R.id.expense_text_view);
        statusTextView = findViewById(R.id.status_text_view);
        progressBar = findViewById(R.id.progress_bar);
        operationsListView = findViewById(R.id.operations_list_view);
        fabAdd = findViewById(R.id.fab_add);

        operations = new ArrayList<>();
        adapter = new OperationAdapter(this, operations);
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
     * Пересчитывает и обновляет баланс
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

        balanceTextView.setText(String.format("₸%.2f", balance));
        incomeTextView.setText(String.format("↑ %.2f", income));
        expenseTextView.setText(String.format("↓ %.2f", expense));
    }

    /**
     * AsyncTask для загрузки операций из SQLite в фоновом потоке
     *
     * Params: Void - не принимает параметры
     * Progress: Integer - прогресс в процентах (0-100)
     * Result: List<Operation> - результат загрузки
     */
    private class LoadOperationsTask extends AsyncTask<Void, Integer, List<Operation>> {

        /**
         * Выполняется в UI потоке перед doInBackground()
         * Подготавливает UI к загрузке
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Показываем ProgressBar
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);

            // Меняем статус
            statusTextView.setText(R.string.status_loading);

            // Отключаем кнопку добавления на время загрузки
            fabAdd.setEnabled(false);
            fabAdd.setAlpha(0.5f);
        }

        /**
         * Выполняется в фоновом потоке
         * Загружает операции из БД с имитацией задержки
         */
        @Override
        protected List<Operation> doInBackground(Void... voids) {
            // Имитируем загрузку данных с прогрессом

            // Начало загрузки - 10%
            publishProgress(10);

            try {
                Thread.sleep(500); // Имитация задержки
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Получение данных из БД - 50%
            publishProgress(50);
            List<Operation> loadedOperations = dbHelper.getAllOperations();

            try {
                Thread.sleep(500); // Имитация обработки
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Обработка завершена - 100%
            publishProgress(100);

            return loadedOperations;
        }

        /**
         * Выполняется в UI потоке при вызове publishProgress()
         * Обновляет прогресс загрузки
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            if (values.length > 0) {
                int progress = values[0];
                progressBar.setProgress(progress);

                // Обновляем статус в зависимости от прогресса
                if (progress < 50) {
                    statusTextView.setText(R.string.status_loading);
                } else if (progress < 100) {
                    statusTextView.setText(R.string.status_processing);
                }
            }
        }

        /**
         * Выполняется в UI потоке после doInBackground()
         * Обновляет UI загруженными данными
         */
        @Override
        protected void onPostExecute(List<Operation> loadedOperations) {
            super.onPostExecute(loadedOperations);

            // Обновляем UI с загруженными данными
            updateUI(loadedOperations);

            // Скрываем ProgressBar
            progressBar.setVisibility(View.GONE);

            // Меняем статус на "Готово"
            statusTextView.setText(R.string.status_ready);

            // Включаем кнопку обратно
            fabAdd.setEnabled(true);
            fabAdd.setAlpha(1.0f);
        }

        /**
         * Выполняется в UI потоке если задача отменена
         */
        @Override
        protected void onCancelled() {
            super.onCancelled();

            // Скрываем ProgressBar
            progressBar.setVisibility(View.GONE);

            // Статус "Отменено"
            statusTextView.setText(R.string.status_cancelled);

            // Включаем кнопку обратно
            fabAdd.setEnabled(true);
            fabAdd.setAlpha(1.0f);
        }
    }
}