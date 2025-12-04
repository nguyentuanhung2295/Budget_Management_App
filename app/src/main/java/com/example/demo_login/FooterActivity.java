package com.example.demo_login;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class FooterActivity extends AppCompatActivity {

    /**
     * @param activity Activity currently
     * @param currentUserId ID user currently logged in.
     */
    public static void setupFooterListeners(final Activity activity, int currentUserId) {

        ImageButton btnMain = activity.findViewById(R.id.footerMain);
        ImageButton btnExpense = activity.findViewById(R.id.footerExpense);
        ImageButton btnReport = activity.findViewById(R.id.footerReport);
        ImageButton btnSetting = activity.findViewById(R.id.footerSetting);

        btnMain.setOnClickListener(v -> navigate(activity, MainActivity.class, currentUserId));

        btnExpense.setOnClickListener(v -> navigate(activity, ExpenseActivity.class, currentUserId));

        btnReport.setOnClickListener(v -> navigate(activity, ReportedActivity.class, currentUserId));

        btnSetting.setOnClickListener(v -> navigate(activity, SettingActivity.class, currentUserId));
    }

    private static void navigate(Activity currentActivity, Class<? extends Activity> targetClass, int userId) {
        if (currentActivity.getClass() != targetClass) {
            Intent intent = new Intent(currentActivity, targetClass);

            intent.putExtra("EXTRA_USER_ID", userId);

            currentActivity.startActivity(intent);
        }
    }
}