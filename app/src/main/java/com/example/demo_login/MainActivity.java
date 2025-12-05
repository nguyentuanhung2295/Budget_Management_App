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

    // --- 1. DECLARE VARIABLES ---
    private TextView tvSelectedDate, tabExpense, tabIncome;
    private EditText etExpenseValue, edtNote;
    private ImageButton btnPrevDay, btnNextDay, btnOpenCalendar;
    private Button btnEnter;
    private GridLayout categoryGridLayout;

    // Logic Variables
    private RadioButton selectedCategoryRadioButton = null;
    private Calendar currentCalendar;
    private boolean isExpenseSelected = true;
    private DatabaseHelper dbHelper;
    private int currentUserId = -1;

    // Edit Mode Variables
    private boolean isEditMode = false;
    private int editingTransactionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- 2. INITIALIZE & MAP VIEWS ---
        currentCalendar = Calendar.getInstance();
        dbHelper = new DatabaseHelper(this);

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

        // --- 3. CHECK LOGIN STATUS ---
        if (getIntent().hasExtra("EXTRA_USER_ID")) {
            currentUserId = getIntent().getIntExtra("EXTRA_USER_ID", -1);
        }
        if (currentUserId == -1) {
            Log.e("AUTH_ERROR", "User ID not found. Redirecting to Login.");
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        // --- 4. SETUP UI & EVENTS ---
        setupCategorySelection();
        updateDateDisplay();
        selectTab(true); // Default to Expense tab

        btnPrevDay.setOnClickListener(v -> navigateDate(-1));
        btnNextDay.setOnClickListener(v -> navigateDate(1));
        btnOpenCalendar.setOnClickListener(v -> showDatePicker());
        tabExpense.setOnClickListener(v -> selectTab(true));
        tabIncome.setOnClickListener(v -> selectTab(false));

        // Handle Save/Update button
        btnEnter.setOnClickListener(v -> handleEnter());

        // Setup Footer Navigation
        FooterActivity.setupFooterListeners(this, currentUserId);

        // Check if in Edit Mode (to pre-fill data)
        checkEditMode();

        // Setup Worker (Runs in background for periodic checks)
        setupRecurringWorker();
    }

    // --- 5. EDIT MODE LOGIC ---
    private void checkEditMode() {
        Intent intent = getIntent();
        if (intent.hasExtra("EDIT_MODE") && intent.getBooleanExtra("EDIT_MODE", false)) {
            isEditMode = true;
            editingTransactionId = intent.getIntExtra("TRANS_ID", -1);

            // Fill Amount
            double amount = intent.getDoubleExtra("TRANS_AMOUNT", 0);
            if(amount == (long) amount)
                etExpenseValue.setText(String.format(Locale.US, "%d", (long)amount));
            else
                etExpenseValue.setText(String.valueOf(amount));

            // Fill Note
            String note = intent.getStringExtra("TRANS_NOTE");
            edtNote.setText(note);

            // Select Tab
            String type = intent.getStringExtra("TRANS_TYPE");
            selectTab(!"income".equalsIgnoreCase(type)); // False if type is Income

            // Select Date
            String dateString = intent.getStringExtra("TRANS_DATE");
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                currentCalendar.setTime(sdf.parse(dateString));
                updateDateDisplay();
            } catch (Exception e) { e.printStackTrace(); }

            // Select Category (Find and simulate click)
            String categoryName = intent.getStringExtra("TRANS_CATEGORY");
            preSelectCategory(categoryName);

            btnEnter.setText("Update");
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

    // --- 6. CATEGORY GRID LOGIC (SINGLE SELECT) ---
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

    // --- 7. SAVE / UPDATE LOGIC (HANDLE ENTER) ---
    private void handleEnter() {
        String amountStr = etExpenseValue.getText().toString().trim();
        String note = edtNote.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter amount!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedCategoryRadioButton == null) {
            Toast.makeText(this, "Please select a category!", Toast.LENGTH_SHORT).show();
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
                success = dbHelper.updateTransaction(
                        editingTransactionId, currentUserId, amount, categoryText, note, transactionDate, transType
                );
            } else {
                success = addTransaction(currentUserId, amount, categoryText, note, transactionDate, transType);
            }
            if (success) {
                String warningMessage = null;
                // Only check budget limit if type is Expense
                if ("expense".equalsIgnoreCase(transType)) {
                    warningMessage = dbHelper.checkAndNotifyBudgetExceeded(currentUserId, categoryText, transactionDate);
                }
                if (warningMessage != null) {
                    // 🚨 Alert exists -> Show Dialog -> Wait for User OK to navigate
                    showBudgetWarningDialog(warningMessage);
                } else {
                    // ✅ No alert -> Toast -> Navigate immediately
                    String msg = isEditMode ? "Updated Successfully!" : "Saved Successfully!";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    resetUIAndNavigate();
                }
            } else {
                Toast.makeText(this, "Database Error!", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid Amount!", Toast.LENGTH_SHORT).show();
        }
    }
    // --- 8. UI HELPER FUNCTIONS ---
    private void showBudgetWarningDialog(String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("⚠️ BUDGET ALERT")
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setNegativeButton("Got it", (dialog, which) -> {
                    dialog.dismiss();
                    resetUIAndNavigate(); // Navigate after user confirms
                })
                .show();
    }

    private void resetUIAndNavigate() {
        etExpenseValue.setText("");
        edtNote.setText("");
        if (selectedCategoryRadioButton != null) {
            selectedCategoryRadioButton.setChecked(false);
            selectedCategoryRadioButton = null;
        }

        isEditMode = false;
        btnEnter.setText("Enter");

        Intent intent = new Intent(MainActivity.this, ExpenseActivity.class);
        intent.putExtra("EXTRA_USER_ID", currentUserId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Close MainActivity to avoid returning to empty form
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

    private void setupRecurringWorker() {
        PeriodicWorkRequest recurringRequest = new PeriodicWorkRequest.Builder(
                RecurringCheckWorker.class,
                24, TimeUnit.HOURS)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "RecurringExpenseCheck",
                ExistingPeriodicWorkPolicy.KEEP,
                recurringRequest
        );
    }

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
            etExpenseValue.setHint("Enter Expense");
        } else {
            tabExpense.setBackgroundResource(R.drawable.tab_unselected_bg);
            tabIncome.setBackgroundResource(R.drawable.tab_selected_bg);
            tabExpense.setTextColor(getResources().getColor(android.R.color.black));
            tabIncome.setTextColor(getResources().getColor(android.R.color.white));
            etExpenseValue.setHint("Enter Income");
        }
    }
}