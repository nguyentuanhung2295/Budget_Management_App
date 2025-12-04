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
        // Get all Active recurring expenses
        List<RecurringExpense> list = dbHelper.getAllActiveRecurring();

        // 1. Format for comparison logic (Date only)
        SimpleDateFormat sdfDateOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        // 2. Format for Notification Display (Date + Time)
        SimpleDateFormat sdfFullTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

        Calendar today = Calendar.getInstance();
        // Reset hour, minute, second for precise date comparison
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        for (RecurringExpense item : list) {
            try {
                Calendar scheduledDate = Calendar.getInstance();
                scheduledDate.setTime(sdfDateOnly.parse(item.getStartDate()));

                // If (Scheduled Date <= Today) -> PAYMENT DUE
                if (!scheduledDate.after(today)) {

                    // 1. Save Transaction
                    dbHelper.addTransaction(
                            item.getUserId(),
                            item.getAmount(),
                            item.getCategory(),
                            "Auto: " + item.getFrequency(),
                            sdfDateOnly.format(today.getTime()),
                            "expense"
                    );

                    // 2. Budget Check
                    dbHelper.checkAndNotifyBudgetExceeded(
                            item.getUserId(),
                            item.getCategory(),
                            sdfDateOnly.format(today.getTime())
                    );

                    // ⭐ FIX: Declare notification content before use ⭐
                    String notifTitle = "💸 Recurring Payment";
                    String notifMsg = "Auto-deducted " + String.format(Locale.US, "%,.0f", item.getAmount()) +
                            " for category " + item.getCategory();

                    // 3. Get real-time for logging
                    String currentTimeStr = sdfFullTime.format(Calendar.getInstance().getTime());

                    // 4. Save notification
                    dbHelper.addNotification(
                            item.getUserId(),
                            notifTitle, // This variable is now declared
                            notifMsg,   // This variable is now declared
                            currentTimeStr
                    );

                    // 5. Calculate next due date
                    if (item.getFrequency().equalsIgnoreCase("Daily")) {
                        scheduledDate.add(Calendar.DAY_OF_YEAR, 1);
                    } else if (item.getFrequency().equalsIgnoreCase("Weekly")) {
                        scheduledDate.add(Calendar.WEEK_OF_YEAR, 1);
                    } else if (item.getFrequency().equalsIgnoreCase("Monthly")) {
                        scheduledDate.add(Calendar.MONTH, 1);
                    } else if (item.getFrequency().equalsIgnoreCase("Yearly")) {
                        scheduledDate.add(Calendar.YEAR, 1);
                    }

                    // 6. Update new date to Database
                    String nextDateStr = sdfDateOnly.format(scheduledDate.getTime());
                    dbHelper.updateRecurringStartDate(item.getId(), nextDateStr);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}