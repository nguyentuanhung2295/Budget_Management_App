package com.example.demo_login;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvDate, tabExpense, tabIncome;
    private EditText etExpenseValue, etNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ Views
        tvDate = findViewById(R.id.tv_selected_date);
        tabExpense = findViewById(R.id.tab_expense);
        tabIncome = findViewById(R.id.tab_income);
        etExpenseValue = findViewById(R.id.et_expense_value);
        etNote = findViewById(R.id.et_note);

        // 1. Thiết lập ngày hiện tại
        setDateToday();

        // 2. Thiết lập sự kiện chuyển Tab
        tabExpense.setOnClickListener(v -> selectTab(true));
        tabIncome.setOnClickListener(v -> selectTab(false));

        // Mặc định chọn Expense khi khởi động
        selectTab(true);
    }

    private void setDateToday() {
        // Định dạng ngày giống trong hình: YYYY.MM.DD (Day)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd (EEE)", Locale.US);
        String currentDate = sdf.format(new Date());
        tvDate.setText(currentDate);
    }

    private void selectTab(boolean isExpense) {
        if (isExpense) {
            // Chọn Expense: Màu đậm và cập nhật nhãn
            tabExpense.setBackgroundResource(R.drawable.tab_selected_bg); // Cần định nghĩa tab_selected_bg
            tabIncome.setBackgroundResource(R.drawable.tab_unselected_bg); // Cần định nghĩa tab_unselected_bg
            tabExpense.setTextColor(getResources().getColor(android.R.color.white));
            tabIncome.setTextColor(getResources().getColor(android.R.color.black)); // Hoặc màu xám
            etExpenseValue.setHint("Nhập Chi phí");

            // Ở đây bạn sẽ thay đổi màu sắc của etExpenseValue nếu cần
        } else {
            // Chọn Income: Màu đậm và cập nhật nhãn
            tabExpense.setBackgroundResource(R.drawable.tab_unselected_bg);
            tabIncome.setBackgroundResource(R.drawable.tab_selected_bg);
            tabExpense.setTextColor(getResources().getColor(android.R.color.black));
            tabIncome.setTextColor(getResources().getColor(android.R.color.white));
            etExpenseValue.setHint("Nhập Thu nhập");
        }

        // Bạn có thể thêm logic để tải danh mục phù hợp (Expense vs Income) ở đây
        Toast.makeText(this, isExpense ? "Chọn Chi tiêu" : "Chọn Thu nhập", Toast.LENGTH_SHORT).show();
    }
}