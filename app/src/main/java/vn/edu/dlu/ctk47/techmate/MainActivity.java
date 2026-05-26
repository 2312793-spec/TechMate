package vn.edu.dlu.ctk47.techmate;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;

import vn.edu.dlu.ctk47.ai.service.FloatingBubbleService;
import vn.edu.dlu.ctk47.techmate.firebase.DataSeeder;

public class MainActivity extends AppCompatActivity {

    private boolean isReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Splash Screen
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        splashScreen.setKeepOnScreenCondition(() -> !isReady);

        try {
            setContentView(R.layout.activity_main);
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            setupNavigation();
        } catch (Exception e) {
            Log.e("TechMate", "UI Setup error: " + e.getMessage());
        }

        // --- QUẢN LÝ VÒNG ĐỜI: Tự động hiện icon khi vào app, ẩn khi thoát app ---
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onStart(@NonNull LifecycleOwner owner) {
                // Khi app quay lại màn hình (Foreground) -> Hiện icon
                if (isReady) startAiAssistant();
            }

            @Override
            public void onStop(@NonNull LifecycleOwner owner) {
                // Khi app thoát ra ngoài (Background/Home) -> Tắt icon hoàn toàn
                stopAiAssistant();
            }
        });

        // 2. Trì hoãn các tác vụ nặng để tránh crash lúc khởi động
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                if (!FirebaseApp.getApps(this).isEmpty()) {
                    DataSeeder.seedInitialData();
                }
                
                // Khởi chạy chatbot lần đầu
                startAiAssistant();
            } catch (Throwable t) {
                Log.e("TechMate", "Initialization error: " + t.getMessage());
            }
            isReady = true;
        }, 1000); 
    }

    private void setupNavigation() {
        try {
            NavHostFragment navHostFragment = (NavHostFragment)
                    getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            if (navHostFragment != null) {
                NavController navController = navHostFragment.getNavController();
                BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
                if (bottomNav != null) {
                    NavigationUI.setupWithNavController(bottomNav, navController);
                }
            }
        } catch (Exception e) {
            Log.e("TechMate", "Navigation error: " + e.getMessage());
        }
    }

    private void startAiAssistant() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                // Chỉ xin quyền nếu app đang mở và chưa có quyền
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 101);
            } else {
                startService(new Intent(this, FloatingBubbleService.class));
            }
        } catch (Throwable t) {
            Log.e("TechMate", "AI Service start error: " + t.getMessage());
        }
    }

    private void stopAiAssistant() {
        try {
            stopService(new Intent(this, FloatingBubbleService.class));
        } catch (Throwable t) {
            Log.e("TechMate", "AI Service stop error: " + t.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                startAiAssistant();
            }
        }
    }
}
