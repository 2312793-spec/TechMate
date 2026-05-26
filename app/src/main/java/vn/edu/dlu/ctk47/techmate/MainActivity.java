package vn.edu.dlu.ctk47.techmate;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import vn.edu.dlu.ctk47.techmate.firebase.DataSeeder;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enable Edge-to-Edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        // Khởi tạo Splash Screen API (mới nhất)
        SplashScreen splash = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Đổ dữ liệu mẫu lên Firebase (Chạy 1 lần rồi comment lại)
        DataSeeder.seedInitialData();

        // Thiết lập tự động ẩn Splash khi app load xong
        splash.setKeepOnScreenCondition(() -> false);
        // Thêm vào onCreate trong MainActivity.java


        // Navigation setup
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
            
            // 🔥 TỰ ĐỘNG KẾT NỐI: Sửa lỗi crash khi click menu
            NavigationUI.setupWithNavController(bottomNav, navController);
        }
    }
}