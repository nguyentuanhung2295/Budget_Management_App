package com.example.demo_login;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Lớp tiện ích để thiết lập các sự kiện click cho thanh điều hướng (Footer).
 * Dùng được cho mọi Activity.
 */
public class FooterActivity extends AppCompatActivity {

    /**
     * Thiết lập các sự kiện chuyển Activity cho các nút trong Footer.
     * @param activity Activity hiện tại (ví dụ: this trong MainActivity).
     * @param currentUserId ID của người dùng đang đăng nhập.
     */
    public static void setupFooterListeners(final Activity activity, int currentUserId) {

        // ⭐ LƯU Ý: Phải gọi findViewById() trên View đã được nhúng Footer
        // Nếu bạn nhúng Footer vào Activity B, thì activity.findViewById() sẽ tìm thấy các ID này.

        // Ánh xạ các nút Footer theo ID trong Layout Footer của bạn
        ImageButton btnMain = activity.findViewById(R.id.footerMain);
        ImageButton btnExpense = activity.findViewById(R.id.footerExpense);
        ImageButton btnReport = activity.findViewById(R.id.footerReport);
        ImageButton btnSetting = activity.findViewById(R.id.footerSetting);

        // 1. Chuyển sang MainActivity
        btnMain.setOnClickListener(v -> navigate(activity, MainActivity.class, currentUserId));

        // 2. Chuyển sang ExpenseActivity
        btnExpense.setOnClickListener(v -> navigate(activity, ExpenseActivity.class, currentUserId));

        // 3. Chuyển sang ReportActivity
        //btnReport.setOnClickListener(v -> navigate(activity, ReportActivity.class, currentUserId));

        // 4. Chuyển sang SettingActivity
        //btnSetting.setOnClickListener(v -> navigate(activity, SettingActivity.class, currentUserId));
    }

    // Hàm chuyển Activity chung
    private static void navigate(Activity currentActivity, Class<? extends Activity> targetClass, int userId) {
        // Tránh chuyển sang chính Activity hiện tại
        if (currentActivity.getClass() != targetClass) {
            Intent intent = new Intent(currentActivity, targetClass);

            // ⭐ QUAN TRỌNG: Gửi kèm User ID sang Activity tiếp theo
            intent.putExtra("EXTRA_USER_ID", userId);

            // ⭐ Tùy chọn: Xóa stack Activity cũ để không quay lại được
            // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            currentActivity.startActivity(intent);
            // Nếu bạn muốn Activity hiện tại đóng lại, bạn có thể gọi currentActivity.finish();
        }
    }
}