package com.example.demo_login;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "BudgetManager.db";
    private static final int DATABASE_VERSION = 1;

    // ----------------------------------------------------------------------
    // 1. DECLARE TABLE AND COLUMN CONSTANTS (CATEGORY REMOVED)
    // ----------------------------------------------------------------------

    // Table 1: USERS
    public static final String TABLE_USER = "User";
    public static final String COL_USER_ID = "userId";
    public static final String COL_USERNAME = "userName";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_VERIFY_CODE = "codeVerify";

    // Table 2: TRANSACTION (MODIFIED: Removed Category FK)
    public static final String TABLE_TRANSACTION = "TransactionTable";
    public static final String COL_TRANS_ID = "transactionId";
    public static final String COL_TRANS_AMOUNT = "amount";
    public static final String COL_TRANS_DESC = "description";
    public static final String COL_TRANS_DATE = "transactionDate";
    public static final String COL_TRANS_TYPE = "type";
    public static final String COL_TRANS_CAT_NAME = "category"; // ⭐ Keep Category Name in transaction table

    // Table 3: BUDGET_LIMIT (MODIFIED: Removed Category FK)
    public static final String TABLE_BUDGET = "BudgetLimit";
    public static final String COL_BUDGET_ID = "budgetLimitId";
    public static final String COL_BUDGET_MAX = "maxAmount";
    public static final String COL_MONTH = "month";
    public static final String COL_YEAR = "year";
    public static final String COL_BUDGET_CAT_NAME = "category"; // ⭐ Keep Category Name in Budget table

    // Table 4: RECURRING_EXPENSE (MODIFIED: Removed Category FK)
    public static final String TABLE_RECURRING = "RecurringExpense";
    public static final String COL_RECUR_ID = "recurringId";
    public static final String COL_RECUR_AMOUNT = "amount";
    public static final String COL_FREQUENCY = "frequency";
    public static final String COL_START_DATE = "startDate";
    public static final String COL_END_DATE = "endDate";
    public static final String COL_STATUS = "status";
    public static final String COL_RECUR_CAT_NAME = "category"; // ⭐ Keep Category Name in Recurring table

    // Table 5: NOTIFICATION
    public static final String TABLE_NOTIFICATION = "Notification";
    public static final String COL_NOTIF_ID = "notificationId";
    public static final String COL_TITLE = "title";
    public static final String COL_MESSAGE = "message";
    public static final String COL_TRIGGER_DATE = "trigger_date";
    public static final String COL_IS_READ = "is_read";

    // ----------------------------------------------------------------------
    // 2. CREATE TABLE STATEMENTS (CATEGORY FOREIGN KEY REMOVED)
    // ----------------------------------------------------------------------

    private static final String CREATE_TABLE_USER =
            "CREATE TABLE " + TABLE_USER + " (" +
                    COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USERNAME + " TEXT, " +
                    COL_EMAIL + " TEXT UNIQUE, " +
                    COL_PASSWORD + " TEXT, " +
                    COL_CREATED_AT + " TEXT, " +
                    COL_VERIFY_CODE + " TEXT" +
                    ")";

    private static final String CREATE_TABLE_TRANSACTION =
            "CREATE TABLE " + TABLE_TRANSACTION + " (" +
                    COL_TRANS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USER_ID + " INTEGER NOT NULL, " +
                    COL_TRANS_CAT_NAME + " TEXT NOT NULL, " + // ⭐ Use Category Name instead of ID
                    COL_TRANS_AMOUNT + " REAL NOT NULL, " +
                    COL_TRANS_DESC + " TEXT, " +
                    COL_TRANS_DATE + " TEXT NOT NULL, " +
                    COL_TRANS_TYPE + " TEXT NOT NULL, " +
                    "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USER + "(" + COL_USER_ID + ")" +
                    ")";

    private static final String CREATE_TABLE_BUDGET =
            "CREATE TABLE " + TABLE_BUDGET + " (" +
                    COL_BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USER_ID + " INTEGER NOT NULL, " +
                    COL_BUDGET_CAT_NAME + " TEXT NOT NULL, " + // ⭐ Use Category Name instead of ID
                    COL_BUDGET_MAX + " REAL NOT NULL, " +
                    COL_MONTH + " INTEGER, " +
                    COL_YEAR + " INTEGER, " +
                    "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USER + "(" + COL_USER_ID + ")" +
                    ")";

    private static final String CREATE_TABLE_RECURRING =
            "CREATE TABLE " + TABLE_RECURRING + " (" +
                    COL_RECUR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USER_ID + " INTEGER NOT NULL, " +
                    COL_RECUR_CAT_NAME + " TEXT NOT NULL, " + // ⭐ Use Category Name instead of ID
                    COL_RECUR_AMOUNT + " REAL NOT NULL, " +
                    COL_FREQUENCY + " TEXT, " +
                    COL_START_DATE + " TEXT NOT NULL, " +
                    COL_END_DATE + " TEXT, " +
                    COL_STATUS + " TEXT, " +
                    "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USER + "(" + COL_USER_ID + ")" +
                    ")";

    private static final String CREATE_TABLE_NOTIFICATION =
            "CREATE TABLE " + TABLE_NOTIFICATION + " (" +
                    COL_NOTIF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USER_ID + " INTEGER NOT NULL, " +
                    COL_TITLE + " TEXT, " +
                    COL_MESSAGE + " TEXT, " +
                    COL_TRIGGER_DATE + " TEXT, " +
                    COL_IS_READ + " INTEGER, " +
                    "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USER + "(" + COL_USER_ID + ")" +
                    ")";

    // ----------------------------------------------------------------------
    // 3. ONCREATE AND ONUPGRADE
    // ----------------------------------------------------------------------

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER);
        db.execSQL(CREATE_TABLE_TRANSACTION);
        db.execSQL(CREATE_TABLE_BUDGET);
        db.execSQL(CREATE_TABLE_RECURRING);
        db.execSQL(CREATE_TABLE_NOTIFICATION);
        Log.d("DB_CREATE", "Successfully created 5 tables.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // ⭐ DROP CATEGORY TABLE AND ALL OTHER TABLES ⭐
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECURRING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGET);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);

        onCreate(db);
        Log.d("DB_UPGRADE", "Upgraded DB to V" + newVersion + ". Category table has been removed.");
    }

    // ----------------------------------------------------------------------
    // 4. CRUD METHODS (FIXED TO USE CORRECT CONSTANTS AND NO CATEGORY ID)
    // ----------------------------------------------------------------------

    // Update password
    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PASSWORD, newPassword);
        String[] whereArgs = {email};
        int rowsAffected = 0;
        try {
            rowsAffected = db.update(TABLE_USER, values, COL_EMAIL + " = ?", whereArgs);
        } finally {
            db.close();
        }
        return rowsAffected > 0;
    }

    // Add new user
    public long addUser(String name, String email, String password, String codePass) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, name);
        values.put(COL_EMAIL, email);
        values.put(COL_PASSWORD, password);
        values.put(COL_VERIFY_CODE, codePass);

        long result = -1;
        try {
            result = db.insert(TABLE_USER, null, values);
        } finally {
            db.close();
        }
        return result;
    }

    // Save verification code
    public boolean setVerifyCode(String email, String code) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_VERIFY_CODE, code);
        String[] whereArgs = {email};
        int rowsAffected = 0;
        try {
            rowsAffected = db.update(TABLE_USER, values, COL_EMAIL + " = ?", whereArgs);
        } finally {
            db.close();
        }
        return rowsAffected > 0;
    }

    // Clear verification code
    public boolean clearVerifyCode(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_VERIFY_CODE, "");
        int rowsAffected = 0;
        try {
            rowsAffected = db.update(TABLE_USER, values, COL_EMAIL + " = ?", new String[]{email});
        } finally {
            db.close();
        }
        return rowsAffected > 0;
    }

    // Check if Email exists
    public boolean checkUserExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE " + COL_EMAIL + " = ?", new String[]{email});
            exists = cursor.getCount() > 0;
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return exists;
    }

    // Check Login
    public int checkCredentials(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {email, password};
        Cursor cursor = null;
        int userId = -1; // Default is failure

        try {
            cursor = db.query(
                    TABLE_USER,
                    new String[]{COL_USER_ID}, // ⭐ Get User ID column
                    COL_EMAIL + " = ? AND " + COL_PASSWORD + " = ?",
                    selectionArgs,
                    null, null, null
            );

            if (cursor.moveToFirst()) {
                // Get userId from the first column
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID));
            }
        } catch (Exception e) {
            Log.e("DB_AUTH", "Login check error: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return userId;
    }

    // Get verification code
    public String getVerifyCode(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String code = null;
        Cursor cursor = null;
        try {
            cursor = db.query(
                    TABLE_USER,
                    new String[]{COL_VERIFY_CODE},
                    COL_EMAIL + " = ?",
                    new String[]{email},
                    null, null, null
            );

            if (cursor.moveToFirst()) {
                code = cursor.getString(cursor.getColumnIndexOrThrow(COL_VERIFY_CODE));
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return code;
    }

    /**
     * Get list of transactions for User in a specific month.
     * @param month Month (1-12)
     * @param year Year (yyyy)
     */
    public List<Transaction> getTransactionsByMonth(int userId, int month, int year) {
        List<Transaction> transactionList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Format month to "01", "02"... for string comparison
        String monthStr = String.format("%02d", month);
        String yearStr = String.valueOf(year);

        // Query: Get all transactions of User, filter by Month and Year from Date string (YYYY-MM-DD)
        // Use strftime to extract month and year from date column
        String query = "SELECT * FROM " + TABLE_TRANSACTION +
                " WHERE " + COL_USER_ID + " = ? " +
                " AND strftime('%m', " + COL_TRANS_DATE + ") = ? " +
                " AND strftime('%Y', " + COL_TRANS_DATE + ") = ? " +
                " ORDER BY " + COL_TRANS_DATE + " DESC"; // Sort newest first

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), monthStr, yearStr});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRANS_ID));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TRANS_AMOUNT));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_CAT_NAME));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_DESC));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_DATE));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_TYPE));

                // Add to list
                transactionList.add(new Transaction(id, amount, category, desc, date, type));
            } while (cursor.moveToNext());
        }

        cursor.close();
        // db.close(); // Keep open if needed immediately
        return transactionList;
    }

    /**
     * Calculate total income/expense in month (Returns double array: [0]=Income, [1]=Expense)
     */
    public double[] getMonthlyTotals(int userId, int month, int year) {
        double income = 0;
        double expense = 0;
        List<Transaction> transactions = getTransactionsByMonth(userId, month, year); // Reuse the function above

        for (Transaction t : transactions) {
            if ("income".equalsIgnoreCase(t.getType())) {
                income += t.getAmount();
            } else {
                expense += t.getAmount();
            }
        }
        return new double[]{income, expense};
    }

    // 1. Delete Transaction Function
    public boolean deleteTransaction(int transactionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // DELETE FROM TransactionTable WHERE transactionId = ?
        int result = db.delete(TABLE_TRANSACTION, COL_TRANS_ID + "=?", new String[]{String.valueOf(transactionId)});
        db.close();
        return result > 0;
    }

    // 2. Update Transaction Function
    public boolean updateTransaction(int transactionId, int userId, double amount, String category, String note, String date, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_USER_ID, userId);
        values.put(COL_TRANS_AMOUNT, amount);
        values.put(COL_TRANS_CAT_NAME, category);
        values.put(COL_TRANS_DESC, note);
        values.put(COL_TRANS_DATE, date);
        values.put(COL_TRANS_TYPE, type);

        // UPDATE TransactionTable SET ... WHERE transactionId = ?
        int result = db.update(TABLE_TRANSACTION, values, COL_TRANS_ID + "=?", new String[]{String.valueOf(transactionId)});
        db.close();
        return result > 0;
    }

    /**
     * Get aggregated total amount by category (for drawing charts).
     * @param type "income" or "expense"
     */
    public Cursor getCategoryReport(int userId, int month, int year, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        String monthStr = String.format("%02d", month);
        String yearStr = String.valueOf(year);

        // SELECT category, SUM(amount) FROM TransactionTable
        // WHERE ... GROUP BY category
        String query = "SELECT " + COL_TRANS_CAT_NAME + ", SUM(" + COL_TRANS_AMOUNT + ") as total " +
                "FROM " + TABLE_TRANSACTION +
                " WHERE " + COL_USER_ID + "=? AND " + COL_TRANS_TYPE + "=?" +
                " AND strftime('%m', " + COL_TRANS_DATE + ") = ? " +
                " AND strftime('%Y', " + COL_TRANS_DATE + ") = ? " +
                " GROUP BY " + COL_TRANS_CAT_NAME;

        return db.rawQuery(query, new String[]{String.valueOf(userId), type, monthStr, yearStr});
    }

    /**
     * Get list of transactions filtered by Type (Income/Expense) for list display.
     */
    public List<Transaction> getTransactionsByType(int userId, int month, int year, String type) {
        List<Transaction> transactionList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String monthStr = String.format("%02d", month);
        String yearStr = String.valueOf(year);

        String query = "SELECT * FROM " + TABLE_TRANSACTION +
                " WHERE " + COL_USER_ID + " = ? " +
                " AND " + COL_TRANS_TYPE + " = ? " + // Add Type filter condition
                " AND strftime('%m', " + COL_TRANS_DATE + ") = ? " +
                " AND strftime('%Y', " + COL_TRANS_DATE + ") = ? " +
                " ORDER BY " + COL_TRANS_DATE + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), type, monthStr, yearStr});

        if (cursor.moveToFirst()) {
            do {
                // ... (Copy Transaction reading logic here) ...
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRANS_ID));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TRANS_AMOUNT));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_CAT_NAME));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_DESC));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_DATE));
                String tType = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_TYPE));
                transactionList.add(new Transaction(id, amount, category, desc, date, tType));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return transactionList;
    }

    // 1. Save Budget Function (Upsert)
    public boolean setBudget(int userId, String category, double maxAmount, int month, int year) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_ID, userId);
        cv.put(COL_BUDGET_CAT_NAME, category);
        cv.put(COL_BUDGET_MAX, maxAmount);
        cv.put(COL_MONTH, month);
        cv.put(COL_YEAR, year);

        // Check if record already exists
        String whereClause = COL_USER_ID + "=? AND " + COL_BUDGET_CAT_NAME + "=? AND " + COL_MONTH + "=? AND " + COL_YEAR + "=?";
        String[] whereArgs = {String.valueOf(userId), category, String.valueOf(month), String.valueOf(year)};

        int rows = db.update(TABLE_BUDGET, cv, whereClause, whereArgs);

        if (rows == 0) {
            // Not exist -> Insert
            long result = db.insert(TABLE_BUDGET, null, cv);
            db.close();
            return result != -1;
        }

        db.close();
        return true; // Update successful
    }

    // 2. Get User's Budget List (New)
    public List<Budget> getBudgets(int userId) {
        List<Budget> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Sort by Year descending, then Month descending
        String query = "SELECT * FROM " + TABLE_BUDGET +
                " WHERE " + COL_USER_ID + " = ? " +
                " ORDER BY " + COL_YEAR + " DESC, " + COL_MONTH + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_BUDGET_ID));
                String cat = cursor.getString(cursor.getColumnIndexOrThrow(COL_BUDGET_CAT_NAME));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_BUDGET_MAX));
                int m = cursor.getInt(cursor.getColumnIndexOrThrow(COL_MONTH));
                int y = cursor.getInt(cursor.getColumnIndexOrThrow(COL_YEAR));

                list.add(new Budget(id, cat, amount, m, y));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
    // Delete Budget by ID
    public boolean deleteBudget(int budgetId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // DELETE FROM BudgetLimit WHERE budgetLimitId = ?
        int result = db.delete(TABLE_BUDGET, COL_BUDGET_ID + "=?", new String[]{String.valueOf(budgetId)});
        db.close();
        return result > 0;
    }
    // ⭐ NEW FUNCTION: Get User info by ID to display in Settings
    public Cursor getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        // SELECT * FROM User WHERE userId = ?
        return db.query(TABLE_USER, null, COL_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
    }

    // 1. Add new recurring expense
    public boolean addRecurring(int userId, String category, double amount, String frequency, String startDate, String endDate, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_ID, userId);
        values.put(COL_RECUR_CAT_NAME, category);
        values.put(COL_RECUR_AMOUNT, amount);
        values.put(COL_FREQUENCY, frequency);
        values.put(COL_START_DATE, startDate);
        values.put(COL_END_DATE, endDate);
        values.put(COL_STATUS, status); // Usually defaults to "Active"

        long result = db.insert(TABLE_RECURRING, null, values);
        db.close();
        return result != -1;
    }

    /**
     * 1. Get all Active recurring expenses for ALL USERS.
     * (Used for background Worker to scan and deduct money)
     */
    public List<RecurringExpense> getAllActiveRecurring() {
        List<RecurringExpense> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query all rows with status Active
        String query = "SELECT * FROM " + TABLE_RECURRING + " WHERE " + COL_STATUS + " = 'Active'";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                // ⭐ IMPORTANT STEP: Must extract data from Cursor to variables first
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RECUR_ID));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)); // Get UserID to know who to deduct from
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_RECUR_CAT_NAME));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_RECUR_AMOUNT));
                String frequency = cursor.getString(cursor.getColumnIndexOrThrow(COL_FREQUENCY));
                String start = cursor.getString(cursor.getColumnIndexOrThrow(COL_START_DATE));
                String end = cursor.getString(cursor.getColumnIndexOrThrow(COL_END_DATE));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS));

                // Create object with full info (including userId)
                list.add(new RecurringExpense(id, userId, category, amount, frequency, start, end, status));
            } while (cursor.moveToNext());
        }
        cursor.close();
        // db.close(); // Keep open if needed
        return list;
    }

    // 3. Delete recurring expense
    public boolean deleteRecurring(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_RECURRING, COL_RECUR_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }

    public void updateRecurringStartDate(int recurringId, String newDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_START_DATE, newDate);
        db.update(TABLE_RECURRING, values, COL_RECUR_ID + "=?", new String[]{String.valueOf(recurringId)});
        db.close();
    }

    /**
     * 2. Get recurring list for A SPECIFIC USER.
     * (Used to display on RecurringActivity screen)
     */
    public List<RecurringExpense> getRecurringList(int userIdInput) {
        List<RecurringExpense> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_RECURRING + " WHERE " + COL_USER_ID + " = ? ORDER BY " + COL_RECUR_ID + " DESC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userIdInput)});

        if (cursor.moveToFirst()) {
            do {
                // Get data from Cursor
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RECUR_ID));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_RECUR_CAT_NAME));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_RECUR_AMOUNT));
                String frequency = cursor.getString(cursor.getColumnIndexOrThrow(COL_FREQUENCY));
                String start = cursor.getString(cursor.getColumnIndexOrThrow(COL_START_DATE));
                String end = cursor.getString(cursor.getColumnIndexOrThrow(COL_END_DATE));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS));

                // Create object
                list.add(new RecurringExpense(id, userId, category, amount, frequency, start, end, status));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public boolean addTransaction(int userId, double amount, String categoryName, String note, String date, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USER_ID, userId);
        values.put(DatabaseHelper.COL_TRANS_AMOUNT, amount);
        values.put(DatabaseHelper.COL_TRANS_CAT_NAME, categoryName);
        values.put(DatabaseHelper.COL_TRANS_DESC, note);
        values.put(DatabaseHelper.COL_TRANS_DATE, date);
        values.put(DatabaseHelper.COL_TRANS_TYPE, type);
        long result = db.insert(DatabaseHelper.TABLE_TRANSACTION, null, values);
        db.close();
        return result != -1;
    }

    //Notification
    // 1. Add New Notification Function
    public void addNotification(int userId, String title, String message, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_ID, userId);
        values.put(COL_TITLE, title);
        values.put(COL_MESSAGE, message);
        values.put(COL_TRIGGER_DATE, date);
        values.put(COL_IS_READ, 0);
        long result = db.insert(TABLE_NOTIFICATION, null, values);
        db.close();
        Log.d("DB_NOTIF", "Added Notification result: " + result);
    }

    // 2. Get Notification List Function
    public List<NotificationItem> getUserNotifications(int userId) {
        List<NotificationItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_NOTIFICATION +
                " WHERE " + COL_USER_ID + " = ? ORDER BY " + COL_NOTIF_ID + " DESC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                String message = cursor.getString(cursor.getColumnIndexOrThrow(COL_MESSAGE));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRIGGER_DATE));
                list.add(new NotificationItem(title, message, date));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // 3. Check Budget Limit Function (Get Max Budget of a category in month)
    public double getBudgetLimit(int userId, String category, int month, int year) {
        SQLiteDatabase db = this.getReadableDatabase();
        double limit = 0;

        String query = "SELECT " + COL_BUDGET_MAX + " FROM " + TABLE_BUDGET +
                " WHERE " + COL_USER_ID + "=? AND " + COL_BUDGET_CAT_NAME + "=? " +
                " AND " + COL_MONTH + "=? AND " + COL_YEAR + "=?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), category, String.valueOf(month), String.valueOf(year)});

        if (cursor.moveToFirst()) {
            limit = cursor.getDouble(0);
        }
        cursor.close();
        return limit; // Return 0 if limit not set
    }

    // 4. Calculate total expense of a category in month (for comparison)
    public double getCategoryTotalExpense(int userId, String category, int month, int year) {
        SQLiteDatabase db = this.getReadableDatabase();
        String monthStr = String.format("%02d", month);

        // Calculate total expense money of that category in the month
        String query = "SELECT SUM(" + COL_TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTION +
                " WHERE " + COL_USER_ID + "=? AND " + COL_TRANS_CAT_NAME + "=? AND " + COL_TRANS_TYPE + "='expense'" +
                " AND strftime('%m', " + COL_TRANS_DATE + ") = ? " +
                " AND strftime('%Y', " + COL_TRANS_DATE + ") = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), category, monthStr, String.valueOf(year)});

        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }
    /**
     * Check limit, save notification to DB and RETURN warning content to display on UI.
     * @return String: Nội dung cảnh báo nếu vượt, null nếu an toàn.
     */
    public String checkAndNotifyBudgetExceeded(int userId, String category, String dateStr) {
        try {
            // 1. Parse date (Assume yyyy-MM-dd format from MainActivity)
            String[] parts = dateStr.split("-");
            if (parts.length < 2) return null; // Avoid error if date format is wrong

            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);

            // 2. Get Budget Limit
            double limit = getBudgetLimit(userId, category, month, year);

            // If budget not set or limit = 0, no check needed
            if (limit <= 0) return null;

            // 3. Calculate actual total expense of category this month (Including newly added item)
            double currentTotal = getCategoryTotalExpense(userId, category, month, year);

            // 4. Compare: If Total Expense > Limit
            if (currentTotal > limit) {
                // Create detailed notification content
                String message = String.format(java.util.Locale.US,
                        "Category '%s' exceeded budget for month %02d/%d.\n\nSpent: %,.0f VND\nLimit: %,.0f VND\nExceeded by: %,.0f VND",
                        category, month, year, currentTotal, limit, (currentTotal - limit));

                // Get current time to save to notification history
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US);
                String now = sdf.format(java.util.Calendar.getInstance().getTime());

                // 5. Save alert to Notification table
                addNotification(userId, "⚠️ Budget Exceeded Alert!", message, now);

                Log.w("BUDGET_CHECK", "User " + userId + " exceeded budget for " + category);

                // ⭐ RETURN MESSAGE TO DISPLAY ON SCREEN (DIALOG)
                return message;
            }

        } catch (Exception e) {
            Log.e("BUDGET_CHECK", "Error checking budget: " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Return null if not exceeding limit or error
    }
}