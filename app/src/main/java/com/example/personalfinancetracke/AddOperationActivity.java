package com.example.personalfinancetracke;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddOperationActivity extends AppCompatActivity {
    
    private EditText amountEditText;
    private RadioGroup typeRadioGroup;
    private Spinner categorySpinner;
    private Button saveButton;
    private DatabaseHelper dbHelper;
    
    private String[] incomeCategories = {"Salary", "Freelance", "Investment", "Other"};
    private String[] expenseCategories = {"Food", "Transport", "Housing", "Entertainment", "Shopping", "Other"};
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_operation);
        
        dbHelper = new DatabaseHelper(this);
        
        amountEditText = findViewById(R.id.amount_edit_text);
        typeRadioGroup = findViewById(R.id.type_radio_group);
        categorySpinner = findViewById(R.id.category_spinner);
        saveButton = findViewById(R.id.save_button);
        
        // Default to expense categories
        updateCategorySpinner(expenseCategories);
        
        typeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_income) {
                    updateCategorySpinner(incomeCategories);
                } else {
                    updateCategorySpinner(expenseCategories);
                }
            }
        });
        
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveOperation();
            }
        });
    }
    
    private void updateCategorySpinner(String[] categories) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, 
            android.R.layout.simple_spinner_item, 
            categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }
    
    private void saveOperation() {
        String amountStr = amountEditText.getText().toString().trim();
        
        if (amountStr.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_amount, Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            double amount = Double.parseDouble(amountStr);
            String type = (typeRadioGroup.getCheckedRadioButtonId() == R.id.radio_income) ? "income" : "expense";
            String category = categorySpinner.getSelectedItem().toString();
            
            dbHelper.addOperation(type, amount, category);
            
            setResult(RESULT_OK);
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.error_empty_amount, Toast.LENGTH_SHORT).show();
        }
    }
}
