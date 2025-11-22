package com.example.demo_login;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "UserAuth.db";
    private static final int DATABASE_VERSION = 2;

    // Tên bảng và tên cột
    public static final String TABLE_USERS = "Users";
    public static final String COL_ID = "ID";
    public static final String COL_EMAIL = "Email";
    public static final String COL_PASSWORD = "Password";
    public static final String COL_VERIFY_CODE = "CodeVerify";

    // Câu lệnh CREATE TABLE
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_EMAIL + " TEXT UNIQUE, " +
                    COL_PASSWORD + " TEXT, " +
                    COL_VERIFY_CODE + " TEXT" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        Log.d("DB_AUTH", "Bảng Users đã được tạo thành công.");

            // ===== BẢNG USER (bạn đã có) =====
            db.execSQL("CREATE TABLE User (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "email TEXT UNIQUE," +
                    "password TEXT)");

            // ===== BẢNG TRANSACTION (nếu bạn đã có thì giữ nguyên) =====
            db.execSQL("CREATE TABLE TransactionTable (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "userId INTEGER," +
                    "amount REAL," +
                    "date TEXT," +
                    "category TEXT," +
                    "note TEXT," +
                    "type TEXT)");

            // ===== THÊM BẢNG BUDGET LIMIT (MỚI) =====
            db.execSQL("CREATE TABLE BudgetLimit (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +    // ID tự tăng
                    "userId INTEGER," +                         // User nào đang dùng hạn mức
                    "category TEXT," +                          // Mục (Food, Transport,...)
                    "maxAmount REAL," +                         // Hạn mức tối đa
                    "month INTEGER," +                          // Tháng áp dụng
                    "year INTEGER)");                           // Năm áp dụng
        }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
        Log.d("DB_AUTH", "CSDL đã nâng cấp và tạo lại bảng Users.");
    }

    // ----------------------------------------------------------------------
    //                   CÁC PHƯƠNG THỨC GHI/CẬP NHẬT DỮ LIỆU
    // ----------------------------------------------------------------------

    // ⭐ PHƯƠNG THỨC BỊ THIẾU: Cập nhật mật khẩu chính thức
    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PASSWORD, newPassword); // Cập nhật cột Password

        String[] whereArgs = {email};
        // Thực hiện lệnh UPDATE
        int rowsAffected = db.update(TABLE_USERS, values, COL_EMAIL + " = ?", whereArgs);
        db.close();
        return rowsAffected > 0;
    }

    // ⭐ Phương thức để thêm người dùng mới
    public long addUser(String email, String password, String codePass) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_EMAIL, email);
        values.put(COL_PASSWORD, password);
        values.put(COL_VERIFY_CODE, codePass); // Giá trị mặc định cho cột CodeVerify

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result;
    }

    // ⭐ Phương thức để lưu/gán mã khôi phục tạm thời
    public boolean setVerifyCode(String email, String code) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_VERIFY_CODE, code);

        String[] whereArgs = {email};
        int rowsAffected = db.update(TABLE_USERS, values, COL_EMAIL + " = ?", whereArgs);
        db.close();
        return rowsAffected > 0;
    }

    // ⭐ Phương thức để xóa mã sau khi cập nhật mật khẩu thành công
    public boolean clearVerifyCode(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_VERIFY_CODE, ""); // Đặt lại giá trị mã xác thực thành rỗng

        int rowsAffected = db.update(TABLE_USERS, values, COL_EMAIL + " = ?", new String[]{email});
        db.close();
        return rowsAffected > 0;
    }

    // ----------------------------------------------------------------------
    //                   CÁC PHƯƠNG THỨC ĐỌC DỮ LIỆU (QUERY)
    // ----------------------------------------------------------------------

    // Phương thức để kiểm tra Email đã tồn tại chưa
    public boolean checkUserExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = ?", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Phương thức để kiểm tra Đăng nhập (Email và Password)
    public boolean checkCredentials(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {email, password};
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_ID},
                COL_EMAIL + " = ? AND " + COL_PASSWORD + " = ?",
                selectionArgs,
                null, null, null
        );
        boolean success = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return success;
    }

    // Phương thức để lấy mã xác thực đã lưu
    public String getVerifyCode(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String code = null;

        // SELECT CodeVerify FROM Users WHERE Email = ?
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_VERIFY_CODE},
                COL_EMAIL + " = ?",
                new String[]{email},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            code = cursor.getString(cursor.getColumnIndexOrThrow(COL_VERIFY_CODE));
        }

        cursor.close();
        db.close();
        return code;
    }
    // ===== Hàm lưu hạn mức chi vào SQLite =====
// Giải thích: userId = người dùng, category = mục chi,
// maxAmount = số tiền giới hạn, month/year = thời gian áp dụng
    public boolean setBudget(int userId, String category, double maxAmount, int month, int year) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("userId", userId);
        cv.put("category", category);
        cv.put("maxAmount", maxAmount);
        cv.put("month", month);
        cv.put("year", year);

        // Insert vào bảng, trả về -1 nếu lỗi
        long result = db.insert("BudgetLimit", null, cv);

        return result != -1; // true = thành công
    }
    // ===== LẤY TỔNG THU TRONG THÁNG =====
    public double getMonthlyIncome(int userId, int month, int year) {

        SQLiteDatabase db = this.getReadableDatabase();

        // Query SUM(amount) WHERE type = income
        @SuppressLint({"Recycle", "DefaultLocale"}) Cursor cursor = db.rawQuery(
                "SELECT SUM(amount) FROM TransactionTable WHERE userId=? AND type='income' " +
                        "AND strftime('%m', date)=? AND strftime('%Y', date)=?",
                new String[]{
                        String.valueOf(userId),
                        String.format("%02d", month),   // chuyển 1 → "01"
                        String.valueOf(year)
                });

        if (cursor.moveToFirst())
            return cursor.getDouble(0);

        return 0;
    }
    // ===== LẤY TỔNG CHI TRONG THÁNG =====
    public double getMonthlyExpense(int userId, int month, int year) {

        SQLiteDatabase db = this.getReadableDatabase();

        @SuppressLint({"Recycle", "DefaultLocale"}) Cursor cursor = db.rawQuery(
                "SELECT SUM(amount) FROM TransactionTable WHERE userId=? AND type='expense' " +
                        "AND strftime('%m', date)=? AND strftime('%Y', date)=?",
                new String[]{
                        String.valueOf(userId),
                        String.format("%02d", month),
                        String.valueOf(year)
                });

        if (cursor.moveToFirst())
            return cursor.getDouble(0);

        return 0;
    }
    // ===== TRẢ VỀ DANH SÁCH GIAO DỊCH TRONG THÁNG =====
    @SuppressLint("DefaultLocale")
    public Cursor getMonthlyTransactions(int userId, int month, int year) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM TransactionTable WHERE userId=? " +
                        "AND strftime('%m', date)=? AND strftime('%Y', date)=? " +
                        "ORDER BY date DESC",
                new String[]{
                        String.valueOf(userId),
                        String.format("%02d", month),
                        String.valueOf(year)
                });
    }


}