package com.example.personalfinancetracke;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
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
    private ListView operationsListView;
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
        operationsListView = findViewById(R.id.operations_list_view);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        
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
        
        loadOperations();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_OPERATION_REQUEST && resultCode == RESULT_OK) {
            loadOperations();
        }
    }
    
    private void loadOperations() {
        operations.clear();
        operations.addAll(dbHelper.getAllOperations());
        adapter.notifyDataSetChanged();
        updateBalance();
    }
    
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
}
