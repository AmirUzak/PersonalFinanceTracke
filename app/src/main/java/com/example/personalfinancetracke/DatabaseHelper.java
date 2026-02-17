package com.example.personalfinancetracke;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "finance_tracker.db";
    private static final int DATABASE_VERSION = 1;
    
    private static final String TABLE_OPERATIONS = "operations";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DATE = "date";
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_OPERATIONS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TYPE + " TEXT NOT NULL, "
                + COLUMN_AMOUNT + " REAL NOT NULL, "
                + COLUMN_CATEGORY + " TEXT NOT NULL, "
                + COLUMN_DATE + " TEXT NOT NULL"
                + ")";
        db.execSQL(CREATE_TABLE);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OPERATIONS);
        onCreate(db);
    }
    
    public long addOperation(String type, double amount, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_DATE, currentDate);
        
        long id = db.insert(TABLE_OPERATIONS, null, values);
        db.close();
        return id;
    }
    
    public List<Operation> getAllOperations() {
        List<Operation> operations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_OPERATIONS, null, null, null, null, null, COLUMN_ID + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                Operation operation = new Operation(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                );
                operations.add(operation);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return operations;
    }
    
    public void deleteOperation(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_OPERATIONS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}
