package com.example.demo_login;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RecurringCheckWorker extends Worker {

    private DatabaseHelper dbHelper;

    public RecurringCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        dbHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        checkAndProcessRecurringExpenses();
        return Result.success();
    }

    private void checkAndProcessRecurringExpenses() {
        // Lấy tất cả các khoản định kỳ đang Active
        List<RecurringExpense> list = dbHelper.getAllActiveRecurring();

        // 1. Định dạng cho Logic so sánh (Chỉ ngày)
        SimpleDateFormat sdfDateOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        // 2. Định dạng cho Hiển thị Thông báo (Ngày + Giờ)
        SimpleDateFormat sdfFullTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

        Calendar today = Calendar.getInstance();
        // Reset giờ phút giây để so sánh ngày chính xác
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        for (RecurringExpense item : list) {
            try {
                Calendar scheduledDate = Calendar.getInstance();
                scheduledDate.setTime(sdfDateOnly.parse(item.getStartDate()));

                // Nếu (Ngày hẹn <= Hôm nay) -> ĐÃ ĐẾN HẠN THANH TOÁN
                if (!scheduledDate.after(today)) {

                    // 1. Lưu Giao Dịch
                    dbHelper.addTransaction(
                            item.getUserId(),
                            item.getAmount(),
                            item.getCategory(),
                            "Auto: " + item.getFrequency(),
                            sdfDateOnly.format(today.getTime()),
                            "expense"
                    );

                    // 2. Kiểm tra hạn mức (Budget Check)
                    dbHelper.checkAndNotifyBudgetExceeded(
                            item.getUserId(),
                            item.getCategory(),
                            sdfDateOnly.format(today.getTime())
                    );

                    // ⭐ SỬA LỖI: Khai báo nội dung thông báo trước khi dùng ⭐
                    String notifTitle = "💸 Thanh toán định kỳ";
                    String notifMsg = "Đã tự động trừ " + String.format(Locale.US, "%,.0f", item.getAmount()) +
                            " cho khoản " + item.getCategory();

                    // 3. Lấy thời gian thực tế để ghi log
                    String currentTimeStr = sdfFullTime.format(Calendar.getInstance().getTime());

                    // 4. Lưu thông báo
                    dbHelper.addNotification(
                            item.getUserId(),
                            notifTitle, // Biến này giờ đã được khai báo
                            notifMsg,   // Biến này giờ đã được khai báo
                            currentTimeStr
                    );

                    // 5. Tính toán ngày tiếp theo (Next Due Date)
                    if (item.getFrequency().equalsIgnoreCase("Daily")) {
                        scheduledDate.add(Calendar.DAY_OF_YEAR, 1);
                    } else if (item.getFrequency().equalsIgnoreCase("Weekly")) {
                        scheduledDate.add(Calendar.WEEK_OF_YEAR, 1);
                    } else if (item.getFrequency().equalsIgnoreCase("Monthly")) {
                        scheduledDate.add(Calendar.MONTH, 1);
                    } else if (item.getFrequency().equalsIgnoreCase("Yearly")) {
                        scheduledDate.add(Calendar.YEAR, 1);
                    }

                    // 6. Cập nhật ngày mới vào Database
                    String nextDateStr = sdfDateOnly.format(scheduledDate.getTime());
                    dbHelper.updateRecurringStartDate(item.getId(), nextDateStr);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}