package com.example.demo_login;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BudgetActivity extends AppCompatActivity {

    Spinner spCategory, spMonth, spYear;
    EditText etBudgetAmount;
    Button btnSave;
    DatabaseHelper db;

    int userId = 1; // TODO: lấy từ session đăng nhập

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_budget); // sử dụng layout LinearLayout

        db = new DatabaseHelper(this);

        // ÁNH XẠ VIEW
        spCategory = findViewById(R.id.spBudgetCategory);
        spMonth = findViewById(R.id.spMonth);
        spYear = findViewById(R.id.spYear);
        etBudgetAmount = findViewById(R.id.etBudgetAmount);
        btnSave = findViewById(R.id.btnSaveBudget);

        // TẢI DỮ LIỆU SPINNER
        loadCategories();
        loadMonth();
        loadYear();

        // SỰ KIỆN NÚT LƯU
        btnSave.setOnClickListener(v -> saveBudget());
    }

    // Load danh mục
    private void loadCategories() {
        String[] categories = {"Ăn uống", "Đi lại", "Học tập", "Mua sắm", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        spCategory.setAdapter(adapter);
    }

    // Tháng (1 → 12)
    private void loadMonth() {
        Integer[] m = {1,2,3,4,5,6,7,8,9,10,11,12};
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, m);
        spMonth.setAdapter(adapter);
    }

    // Năm (2024–2026)
    private void loadYear() {
        Integer[] y = {2024, 2025, 2026};
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, y);
        spYear.setAdapter(adapter);
    }

    // LƯU HẠN MỨC
    private void saveBudget() {
        String category = spCategory.getSelectedItem().toString();
        String amountStr = etBudgetAmount.getText().toString();

        if (amountStr.isEmpty()) {
            etBudgetAmount.setError("Vui lòng nhập số tiền!");
            return;
        }

        double amount = Double.parseDouble(amountStr);
        int month = (int) spMonth.getSelectedItem();
        int year = (int) spYear.getSelectedItem();

        boolean ok = db.setBudget(userId, category, amount, month, year);

        if (ok) {
            Toast.makeText(this, "Đã lưu hạn mức!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Lỗi lưu dữ liệu!", Toast.LENGTH_SHORT).show();
        }
    }
}