package com.example.demo_login;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReportedActivity extends AppCompatActivity implements TransactionAdapter.OnTransactionItemListener {

    // Views
    private TextView tvSelectedMonth, tvTotalIncome, tvTotalExpense, tvBalance;
    private TextView tvFilterExpense, tvFilterIncome; // Nút lọc
    private ImageButton btnPrevMonth, btnNextMonth, btnOpenCalendar;
    private RecyclerView recyclerView;
    private PieChart pieChart; // Biểu đồ

    // Logic
    private DatabaseHelper dbHelper;
    private int currentUserId = -1;
    private Calendar currentCalendar;
    private TransactionAdapter adapter;

    // Trạng thái lọc hiện tại ("expense" hoặc "income")
    private String currentFilterType = "expense";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reported);

        // Khởi tạo
        currentCalendar = Calendar.getInstance();
        dbHelper = new DatabaseHelper(this);

        // Ánh xạ Views (CŨ + MỚI)
        tvSelectedMonth = findViewById(R.id.tv_selected_month);
        btnPrevMonth = findViewById(R.id.btn_prev_month);
        btnNextMonth = findViewById(R.id.btn_next_month);
        btnOpenCalendar = findViewById(R.id.btn_open_calendar);

        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvTotalExpense = findViewById(R.id.tv_total_expense);
        tvBalance = findViewById(R.id.tv_balance);

        recyclerView = findViewById(R.id.recycler_transactions);
        tvFilterExpense = findViewById(R.id.tv_filter_expense);
        tvFilterIncome = findViewById(R.id.tv_filter_income);
        pieChart = findViewById(R.id.pieChart);

        // Nhận User ID
        if (getIntent().hasExtra("EXTRA_USER_ID")) {
            currentUserId = getIntent().getIntExtra("EXTRA_USER_ID", -1);
        }

        // Cấu hình RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Thiết lập Footer
        FooterActivity.setupFooterListeners(this, currentUserId);

        // Sự kiện Lọc Income/Expense
        tvFilterExpense.setOnClickListener(v -> setFilter("expense"));
        tvFilterIncome.setOnClickListener(v -> setFilter("income"));

        // Sự kiện Calendar
        btnPrevMonth.setOnClickListener(v -> navigateMonth(-1));
        btnNextMonth.setOnClickListener(v -> navigateMonth(1));
        btnOpenCalendar.setOnClickListener(v -> showMonthPicker());

        // Load dữ liệu lần đầu
        updateMonthDisplay();

        // Đặt màu mặc định cho nút filter ban đầu (expense)
        setFilter("expense");
    }

    // --- LOGIC CHUYỂN ĐỔI BỘ LỌC ---
    private void setFilter(String type) {
        currentFilterType = type;

        // Cập nhật UI nút bấm (Thay đổi màu nền/chữ để biết đang chọn cái nào)
        if ("expense".equals(type)) {
            // Expense Active
            tvFilterExpense.setBackgroundResource(R.drawable.tab_selected_bg);
            tvFilterExpense.setTextColor(Color.WHITE);
            // Income Inactive
            tvFilterIncome.setBackgroundResource(R.drawable.tab_unselected_bg);
            tvFilterIncome.setTextColor(Color.BLACK);
        } else {
            // Income Active
            tvFilterIncome.setBackgroundResource(R.drawable.tab_selected_bg);
            tvFilterIncome.setTextColor(Color.WHITE);
            // Expense Inactive
            tvFilterExpense.setBackgroundResource(R.drawable.tab_unselected_bg);
            tvFilterExpense.setTextColor(Color.BLACK);
        }

        // Tải lại dữ liệu (Biểu đồ + List) theo bộ lọc mới
        loadDataForMonth();
    }

    private void loadDataForMonth() {
        int month = currentCalendar.get(Calendar.MONTH) + 1;
        int year = currentCalendar.get(Calendar.YEAR);

        // 1. Cập nhật Dashboard (Tổng quan) - Không phụ thuộc bộ lọc
        updateDashboard(month, year);

        // 2. Cập nhật Danh sách Giao dịch (Theo bộ lọc hiện tại)
        List<Transaction> list = dbHelper.getTransactionsByType(currentUserId, month, year, currentFilterType);

        if (adapter == null) {
            adapter = new TransactionAdapter(this, list, this);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(list);
        }

        // 3. Vẽ Biểu đồ Tròn (Theo bộ lọc hiện tại)
        drawPieChart(month, year, currentFilterType);
    }

    // ⭐ HÀM CẬP NHẬT DASHBOARD (Đã bổ sung) ⭐
    private void updateDashboard(int month, int year) {
        double[] totals = dbHelper.getMonthlyTotals(currentUserId, month, year);
        double income = totals[0];
        double expense = totals[1];
        double balance = income - expense;

        NumberFormat fmt = NumberFormat.getInstance(Locale.US);
        tvTotalIncome.setText(fmt.format(income));
        tvTotalExpense.setText(fmt.format(expense));
        tvBalance.setText(fmt.format(balance));
    }

    // --- HÀM VẼ BIỂU ĐỒ ---
    private void drawPieChart(int month, int year, String type) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        // Gọi hàm lấy báo cáo theo category từ DB
        Cursor cursor = dbHelper.getCategoryReport(currentUserId, month, year, type);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Cột 0 là tên Category, Cột 1 là Tổng tiền (SUM)
                String category = cursor.getString(0);
                float amount = cursor.getFloat(1);
                entries.add(new PieEntry(amount, category));
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (entries.isEmpty()) {
            pieChart.clear(); // Xóa biểu đồ nếu không có dữ liệu
            pieChart.setNoDataText("Chưa có dữ liệu " + type + " trong tháng này.");
            pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, type.toUpperCase());
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Bộ màu mặc định
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText(type.toUpperCase());
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.animateY(1000);
        pieChart.invalidate(); // Refresh biểu đồ
    }

    // --- CÁC HÀM LOGIC NGÀY THÁNG ---

    private void updateMonthDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", Locale.US);
        tvSelectedMonth.setText(sdf.format(currentCalendar.getTime()));
        // Không gọi loadDataForMonth() ở đây nữa vì setFilter() trong onCreate đã gọi rồi
    }

    private void navigateMonth(int months) {
        currentCalendar.add(Calendar.MONTH, months);
        updateMonthDisplay();
        loadDataForMonth(); // Gọi load data khi đổi tháng
    }

    private void showMonthPicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    currentCalendar.set(Calendar.YEAR, year);
                    currentCalendar.set(Calendar.MONTH, month);
                    currentCalendar.set(Calendar.DAY_OF_MONTH, 1);

                    updateMonthDisplay();
                    loadDataForMonth(); // Gọi load data khi chọn xong
                },
                currentCalendar.get(Calendar.YEAR),
                currentCalendar.get(Calendar.MONTH),
                currentCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    // --- TRIỂN KHAI INTERFACE ADAPTER (DELETE/EDIT) ---

    @Override
    public void onDeleteClick(int transactionId, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean isDeleted = dbHelper.deleteTransaction(transactionId);
                    if (isDeleted) {
                        Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                        loadDataForMonth(); // Reload data
                    } else {
                        Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onEditClick(Transaction transaction) {
        Intent intent = new Intent(ReportedActivity.this, MainActivity.class);
        intent.putExtra("EXTRA_USER_ID", currentUserId);

        // Gửi dữ liệu sang Main để Edit
        intent.putExtra("EDIT_MODE", true);
        intent.putExtra("TRANS_ID", transaction.getId());
        intent.putExtra("TRANS_AMOUNT", transaction.getAmount());
        intent.putExtra("TRANS_CATEGORY", transaction.getCategoryName());
        intent.putExtra("TRANS_NOTE", transaction.getDescription());
        intent.putExtra("TRANS_DATE", transaction.getDate());
        intent.putExtra("TRANS_TYPE", transaction.getType());

        startActivity(intent);
    }
}