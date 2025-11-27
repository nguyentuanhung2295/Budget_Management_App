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
    // 1. KHAI BÁO HẰNG SỐ CỘT VÀ BẢNG (ĐÃ BỎ CATEGORY)
    // ----------------------------------------------------------------------

    // Bảng 1: USERS
    public static final String TABLE_USER = "User";
    public static final String COL_USER_ID = "userId";
    public static final String COL_USERNAME = "userName";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_VERIFY_CODE = "codeVerify";

    // Bảng 2: TRANSACTION (SỬA LẠI: Loại bỏ FK Category)
    public static final String TABLE_TRANSACTION = "TransactionTable";
    public static final String COL_TRANS_ID = "transactionId";
    public static final String COL_TRANS_AMOUNT = "amount";
    public static final String COL_TRANS_DESC = "description";
    public static final String COL_TRANS_DATE = "transactionDate";
    public static final String COL_TRANS_TYPE = "type";
    public static final String COL_TRANS_CAT_NAME = "category"; // ⭐ Giữ tên Category trong bảng giao dịch

    // Bảng 3: BUDGET_LIMIT (SỬA LẠI: Loại bỏ FK Category)
    public static final String TABLE_BUDGET = "BudgetLimit";
    public static final String COL_BUDGET_ID = "budgetLimitId";
    public static final String COL_BUDGET_MAX = "maxAmount";
    public static final String COL_MONTH = "month";
    public static final String COL_YEAR = "year";
    public static final String COL_BUDGET_CAT_NAME = "category"; // ⭐ Giữ tên Category trong bảng Budget

    // Bảng 4: RECURRING_EXPENSE (SỬA LẠI: Loại bỏ FK Category)
    public static final String TABLE_RECURRING = "RecurringExpense";
    public static final String COL_RECUR_ID = "recurringId";
    public static final String COL_RECUR_AMOUNT = "amount";
    public static final String COL_FREQUENCY = "frequency";
    public static final String COL_START_DATE = "startDate";
    public static final String COL_END_DATE = "endDate";
    public static final String COL_STATUS = "status";
    public static final String COL_RECUR_CAT_NAME = "category"; // ⭐ Giữ tên Category trong bảng Recurring

    // Bảng 5: NOTIFICATION
    public static final String TABLE_NOTIFICATION = "Notification";
    public static final String COL_NOTIF_ID = "notificationId";
    public static final String COL_TITLE = "title";
    public static final String COL_MESSAGE = "message";
    public static final String COL_TRIGGER_DATE = "trigger_date";
    public static final String COL_IS_READ = "is_read";

    // ----------------------------------------------------------------------
    // 2. CÂU LỆNH CREATE TABLE (ĐÃ LOẠI BỎ KHÓA NGOẠI CATEGORY)
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
                    COL_TRANS_CAT_NAME + " TEXT NOT NULL, " + // ⭐ Dùng tên Category thay cho ID
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
                    COL_BUDGET_CAT_NAME + " TEXT NOT NULL, " + // ⭐ Dùng tên Category thay cho ID
                    COL_BUDGET_MAX + " REAL NOT NULL, " +
                    COL_MONTH + " INTEGER, " +
                    COL_YEAR + " INTEGER, " +
                    "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USER + "(" + COL_USER_ID + ")" +
                    ")";

    private static final String CREATE_TABLE_RECURRING =
            "CREATE TABLE " + TABLE_RECURRING + " (" +
                    COL_RECUR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USER_ID + " INTEGER NOT NULL, " +
                    COL_RECUR_CAT_NAME + " TEXT NOT NULL, " + // ⭐ Dùng tên Category thay cho ID
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
    // 3. ONCREATE VÀ ONUPGRADE
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
        Log.d("DB_CREATE", "Đã tạo 5 bảng thành công.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // ⭐ XÓA BẢNG CATEGORY VÀ TẤT CẢ CÁC BẢNG KHÁC ⭐
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECURRING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGET);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);

        onCreate(db);
        Log.d("DB_UPGRADE", "Đã nâng cấp CSDL lên V" + newVersion + ". Bảng Category đã bị loại bỏ.");
    }

    // ----------------------------------------------------------------------
    // 4. CÁC PHƯƠNG THỨC CRUD (ĐÃ SỬA DÙNG ĐÚNG HẰNG SỐ VÀ KHÔNG CẦN CATEGORY ID)
    // ----------------------------------------------------------------------

    // Cập nhật mật khẩu
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

    // Thêm người dùng mới
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

    // Lưu mã xác thực
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

    // Xóa mã xác thực
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

    // Kiểm tra Email tồn tại
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
        int userId = -1; // Mặc định là thất bại

        try {
            cursor = db.query(
                    TABLE_USER,
                    new String[]{COL_USER_ID}, // ⭐ Lấy cột User ID
                    COL_EMAIL + " = ? AND " + COL_PASSWORD + " = ?",
                    selectionArgs,
                    null, null, null
            );

            if (cursor.moveToFirst()) {
                // Lấy userId từ cột đầu tiên
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID));
            }
        } catch (Exception e) {
            Log.e("DB_AUTH", "Lỗi kiểm tra đăng nhập: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return userId;
    }

    // Lấy mã xác thực
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
     * Lấy danh sách giao dịch của User trong một tháng cụ thể.
     * @param month Tháng (1-12)
     * @param year Năm (yyyy)
     */
    public List<Transaction> getTransactionsByMonth(int userId, int month, int year) {
        List<Transaction> transactionList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Định dạng tháng thành "01", "02"... để so sánh chuỗi
        String monthStr = String.format("%02d", month);
        String yearStr = String.valueOf(year);

        // Truy vấn: Lấy tất cả giao dịch của User, lọc theo Tháng và Năm từ chuỗi Date (YYYY-MM-DD)
        // Sử dụng strftime để trích xuất tháng và năm từ cột date
        String query = "SELECT * FROM " + TABLE_TRANSACTION +
                " WHERE " + COL_USER_ID + " = ? " +
                " AND strftime('%m', " + COL_TRANS_DATE + ") = ? " +
                " AND strftime('%Y', " + COL_TRANS_DATE + ") = ? " +
                " ORDER BY " + COL_TRANS_DATE + " DESC"; // Sắp xếp mới nhất lên đầu

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), monthStr, yearStr});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRANS_ID));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TRANS_AMOUNT));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_CAT_NAME));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_DESC));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_DATE));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_TYPE));

                // Thêm vào danh sách
                transactionList.add(new Transaction(id, amount, category, desc, date, type));
            } while (cursor.moveToNext());
        }

        cursor.close();
        // db.close(); // Giữ mở nếu cần dùng lại ngay
        return transactionList;
    }

    /**
     * Tính tổng thu/chi trong tháng (Trả về mảng double: [0]=Income, [1]=Expense)
     */
    public double[] getMonthlyTotals(int userId, int month, int year) {
        double income = 0;
        double expense = 0;
        List<Transaction> transactions = getTransactionsByMonth(userId, month, year); // Tái sử dụng hàm trên

        for (Transaction t : transactions) {
            if ("income".equalsIgnoreCase(t.getType())) {
                income += t.getAmount();
            } else {
                expense += t.getAmount();
            }
        }
        return new double[]{income, expense};
    }

    // 1. Hàm Xóa Giao Dịch
    public boolean deleteTransaction(int transactionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // DELETE FROM TransactionTable WHERE transactionId = ?
        int result = db.delete(TABLE_TRANSACTION, COL_TRANS_ID + "=?", new String[]{String.valueOf(transactionId)});
        db.close();
        return result > 0;
    }

    // 2. Hàm Cập Nhật Giao Dịch
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
}