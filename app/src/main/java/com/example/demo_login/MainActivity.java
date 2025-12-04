package com.example.demo_login;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import java.util.concurrent.TimeUnit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Khai báo Views
    private TextView tvSelectedDate, tabExpense, tabIncome;
    private EditText etExpenseValue, edtNote;
    private ImageButton btnPrevDay, btnNextDay, btnOpenCalendar;
    private Button btnEnter;
    private GridLayout categoryGridLayout;
    private RadioButton selectedCategoryRadioButton = null;

    // Khai báo cho Logic
    private Calendar currentCalendar;
    private boolean isExpenseSelected = true;
    private DatabaseHelper dbHelper;
    private int currentUserId = -1;

    // ⭐ Biến cho chế độ Edit
    private boolean isEditMode = false;
    private int editingTransactionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo
        currentCalendar = Calendar.getInstance();
        dbHelper = new DatabaseHelper(this);

        // Ánh xạ Views UI
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        tabExpense = findViewById(R.id.tab_expense);
        tabIncome = findViewById(R.id.tab_income);
        etExpenseValue = findViewById(R.id.et_expense_value);
        edtNote = findViewById(R.id.edtNote);
        btnEnter = findViewById(R.id.btnEnter);
        categoryGridLayout = findViewById(R.id.category_grid_layout);
        btnPrevDay = findViewById(R.id.btn_prev_day);
        btnNextDay = findViewById(R.id.btn_next_day);
        btnOpenCalendar = findViewById(R.id.btn_open_calendar);

        // 1. Nhận User ID từ Intent và kiểm tra
        if (getIntent().hasExtra("EXTRA_USER_ID")) {
            currentUserId = getIntent().getIntExtra("EXTRA_USER_ID", -1);
        }
        if (currentUserId == -1) {
            Log.e("AUTH_ERROR", "Không tìm thấy User ID trong MainActivity. Chuyển về Login.");
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        // 2. Thiết lập các sự kiện và hiển thị
        setupCategorySelection();
        updateDateDisplay();
        selectTab(true); // Mặc định chọn Expense

        btnPrevDay.setOnClickListener(v -> navigateDate(-1));
        btnNextDay.setOnClickListener(v -> navigateDate(1));
        btnOpenCalendar.setOnClickListener(v -> showDatePicker());
        tabExpense.setOnClickListener(v -> selectTab(true));
        tabIncome.setOnClickListener(v -> selectTab(false));
        btnEnter.setOnClickListener(v -> handleEnter());

        // 3. Thiết lập Footer
        FooterActivity.setupFooterListeners(this, currentUserId);

        // ⭐ 4. Kiểm tra xem có phải đang Edit không
        checkEditMode();

        // ⭐ THIẾT LẬP WORKER CHẠY NGẦM ⭐
        // Worker sẽ chạy ít nhất mỗi 15 phút (giới hạn nhỏ nhất của Android) để kiểm tra
        // Tuy nhiên, logic bên trong Worker sẽ chỉ trừ tiền nếu ngày hiện tại >= ngày hẹn.

        PeriodicWorkRequest recurringRequest = new PeriodicWorkRequest.Builder(
                RecurringCheckWorker.class,
                24, TimeUnit.HOURS) // Kiểm tra mỗi 24 giờ
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "RecurringExpenseCheck",
                ExistingPeriodicWorkPolicy.KEEP, // Nếu đã có lịch rồi thì giữ nguyên, không tạo mới
                recurringRequest
        );
    }

    // --- LOGIC EDIT ---
    private void checkEditMode() {
        Intent intent = getIntent();
        if (intent.hasExtra("EDIT_MODE") && intent.getBooleanExtra("EDIT_MODE", false)) {
            isEditMode = true;
            editingTransactionId = intent.getIntExtra("TRANS_ID", -1);

            // 1. Điền số tiền
            double amount = intent.getDoubleExtra("TRANS_AMOUNT", 0);
            if(amount == (long) amount)
                etExpenseValue.setText(String.format(Locale.US, "%d", (long)amount));
            else
                etExpenseValue.setText(String.valueOf(amount));

            // 2. Điền ghi chú
            String note = intent.getStringExtra("TRANS_NOTE");
            edtNote.setText(note);

            // 3. Chọn Tab
            String type = intent.getStringExtra("TRANS_TYPE");
            if ("income".equalsIgnoreCase(type)) {
                selectTab(false);
            } else {
                selectTab(true);
            }

            // 4. Chọn Ngày
            String dateString = intent.getStringExtra("TRANS_DATE");
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                currentCalendar.setTime(sdf.parse(dateString));
                updateDateDisplay();
            } catch (Exception e) { e.printStackTrace(); }

            // 5. Chọn Category
            String categoryName = intent.getStringExtra("TRANS_CATEGORY");
            preSelectCategory(categoryName);

            btnEnter.setText("Update"); // Đổi tên nút
        }
    }

    private void preSelectCategory(String categoryName) {
        for (int i = 0; i < categoryGridLayout.getChildCount(); i++) {
            View child = categoryGridLayout.getChildAt(i);
            if (child instanceof RadioButton) {
                RadioButton rb = (RadioButton) child;
                if (rb.getText().toString().equals(categoryName)) {
                    handleSingleSelection(rb);
                    break;
                }
            }
        }
    }

    // --- CÁC HÀM UI CŨ ---
    private void setupCategorySelection() {
        for (int i = 0; i < categoryGridLayout.getChildCount(); i++) {
            View child = categoryGridLayout.getChildAt(i);
            if (child instanceof RadioButton) {
                RadioButton rb = (RadioButton) child;
                rb.setOnClickListener(v -> handleSingleSelection(rb));
            }
        }
    }

    private void handleSingleSelection(RadioButton clickedRadioButton) {
        if (selectedCategoryRadioButton != null) {
            selectedCategoryRadioButton.setChecked(false);
        }
        selectedCategoryRadioButton = clickedRadioButton;
        selectedCategoryRadioButton.setChecked(true);
    }

    // ⭐ HÀM XỬ LÝ NÚT ENTER (ĐÃ SỬA LOGIC CHUYỂN TRANG)
    private void handleEnter() {
        String amountStr = etExpenseValue.getText().toString().trim();
        String note = edtNote.getText().toString().trim();

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedCategoryRadioButton == null) {
            Toast.makeText(this, "Vui lòng chọn danh mục!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            String categoryText = selectedCategoryRadioButton.getText().toString();
            String transType = isExpenseSelected ? "expense" : "income";
            SimpleDateFormat sdfDB = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            String transactionDate = sdfDB.format(currentCalendar.getTime());

            boolean success;
            if (isEditMode) {
                // Gọi hàm UPDATE
                success = dbHelper.updateTransaction(
                        editingTransactionId,
                        currentUserId,
                        amount,
                        categoryText,
                        note,
                        transactionDate,
                        transType
                );
            } else {
                // Gọi hàm INSERT (Cũ)
                success = addTransaction(currentUserId, amount, categoryText, note, transactionDate, transType);
            }

            if (success) {
                String warningMessage = null;

                // 1. Kiểm tra hạn mức (Chỉ nếu là Expense)
                if ("expense".equalsIgnoreCase(transType)) {
                    warningMessage = dbHelper.checkAndNotifyBudgetExceeded(currentUserId, categoryText, transactionDate);
                }

                // 2. Xử lý hiển thị và điều hướng
                if (warningMessage != null) {
                    // 🚨 TRƯỜNG HỢP CÓ CẢNH BÁO:
                    // Hiện Dialog và KHÔNG chuyển trang ngay lập tức.
                    // Việc chuyển trang sẽ được thực hiện khi người dùng bấm nút trong Dialog.
                    showBudgetWarningDialog(warningMessage);
                } else {
                    // ✅ TRƯỜNG HỢP BÌNH THƯỜNG:
                    // Hiện Toast và chuyển trang ngay lập tức.
                    String msg = isEditMode ? "Cập nhật thành công!" : "Lưu thành công!";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    resetUIAndNavigate();
                }
            } else {
                Toast.makeText(this, "Lỗi khi lưu/cập nhật.", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ.", Toast.LENGTH_SHORT).show();
        }
    }

    // ⭐ HÀM HIỂN THỊ CẢNH BÁO (ĐÃ SỬA)
    private void showBudgetWarningDialog(String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("⚠️ CẢNH BÁO VƯỢT HẠN MỨC")
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false) // Bắt buộc người dùng phải tương tác với nút
                .setNegativeButton("Đã hiểu", (dialog, which) -> {
                    dialog.dismiss();
                    // ⭐ CHUYỂN TRANG TẠI ĐÂY (Sau khi người dùng đã đọc và bấm nút)
                    resetUIAndNavigate();
                })
                .show();
    }

    // ⭐ HÀM PHỤ TRỢ: RESET UI VÀ CHUYỂN TRANG
    private void resetUIAndNavigate() {
        // 1. Xóa dữ liệu trên Form
        etExpenseValue.setText("");
        edtNote.setText("");
        if (selectedCategoryRadioButton != null) {
            selectedCategoryRadioButton.setChecked(false);
            selectedCategoryRadioButton = null;
        }

        isEditMode = false;
        btnEnter.setText("Enter");

        // 2. Chuyển sang màn hình ExpenseActivity
        Intent intent = new Intent(MainActivity.this, ExpenseActivity.class);
        intent.putExtra("EXTRA_USER_ID", currentUserId);
        // Xóa cờ history để khi bấm Back ở màn hình kia không quay lại form nhập liệu này
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private boolean addTransaction(int userId, double amount, String categoryName, String note, String date, String type) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
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

    // ... (Các hàm updateDateDisplay, navigateDate, showDatePicker, selectTab giữ nguyên) ...
    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd (EEE)", Locale.US);
        tvSelectedDate.setText(sdf.format(currentCalendar.getTime()));
    }

    private void navigateDate(int days) {
        currentCalendar.add(Calendar.DAY_OF_YEAR, days);
        updateDateDisplay();
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    currentCalendar.set(year, month, dayOfMonth);
                    updateDateDisplay();
                },
                currentCalendar.get(Calendar.YEAR),
                currentCalendar.get(Calendar.MONTH),
                currentCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void selectTab(boolean isExpense) {
        isExpenseSelected = isExpense;
        if (isExpense) {
            tabExpense.setBackgroundResource(R.drawable.tab_selected_bg);
            tabIncome.setBackgroundResource(R.drawable.tab_unselected_bg);
            tabExpense.setTextColor(getResources().getColor(android.R.color.white));
            tabIncome.setTextColor(getResources().getColor(android.R.color.black));
            etExpenseValue.setHint("Nhập Chi phí");
        } else {
            tabExpense.setBackgroundResource(R.drawable.tab_unselected_bg);
            tabIncome.setBackgroundResource(R.drawable.tab_selected_bg);
            tabExpense.setTextColor(getResources().getColor(android.R.color.black));
            tabIncome.setTextColor(getResources().getColor(android.R.color.white));
            etExpenseValue.setHint("Nhập Thu nhập");
        }
    }
}