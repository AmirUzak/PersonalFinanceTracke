package com.example.personalfinancetracke;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import java.util.List;

public class OperationAdapter extends ArrayAdapter<Operation> {
    
    public OperationAdapter(Context context, List<Operation> operations) {
        super(context, 0, operations);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Operation operation = getItem(position);
        
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_operation, parent, false);
        }
        
        TextView categoryTextView = convertView.findViewById(R.id.category_text_view);
        TextView amountTextView = convertView.findViewById(R.id.amount_text_view);
        TextView typeTextView = convertView.findViewById(R.id.type_text_view);
        TextView dateTextView = convertView.findViewById(R.id.date_text_view);
        
        categoryTextView.setText(operation.getCategory());
        amountTextView.setText(String.format("₸%.2f", operation.getAmount()));
        typeTextView.setText(operation.getType().equals("income") ? 
            getContext().getString(R.string.income) : getContext().getString(R.string.expense));
        dateTextView.setText(operation.getDate());
        
        // Color coding
        if (operation.getType().equals("income")) {
            amountTextView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_green_dark));
        } else {
            amountTextView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark));
        }
        
        return convertView;
    }
}
