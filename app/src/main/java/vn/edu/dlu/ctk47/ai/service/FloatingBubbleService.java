package vn.edu.dlu.ctk47.ai.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import vn.edu.dlu.ctk47.techmate.BuildConfig;
import vn.edu.dlu.ctk47.techmate.R;
import vn.edu.dlu.ctk47.ai.api.AiApiClient;
import vn.edu.dlu.ctk47.ai.api.AiApiService;
import vn.edu.dlu.ctk47.ai.model.AiChatRequest;
import vn.edu.dlu.ctk47.ai.model.AiChatResponse;
import vn.edu.dlu.ctk47.ai.model.ChatMessage;
import vn.edu.dlu.ctk47.ai.model.ChatAdapter;

public class FloatingBubbleService extends Service {
    private WindowManager windowManager;
    private View floatingView;
    private View chatWindowView;
    private WindowManager.LayoutParams params;
    private boolean isChatWindowVisible = false;

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private ImageButton btnSend;
    private static List<ChatMessage> messageList = new ArrayList<>();
    private Context themedContext;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        themedContext = new ContextThemeWrapper(this, R.style.Theme_TechMate);
        setupFloatingBubble();
    }

    private void setupFloatingBubble() {
        try {
            floatingView = LayoutInflater.from(themedContext).inflate(R.layout.floating_bubble, null);

            int layoutType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                    ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    : WindowManager.LayoutParams.TYPE_PHONE;

            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    layoutType,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            params.gravity = Gravity.BOTTOM | Gravity.END;
            params.x = 50;
            params.y = 150;

            windowManager.addView(floatingView, params);
            setupBubbleTouch();
        } catch (Exception e) {
            Log.e("TechMate", "Lỗi tạo Bubble: " + e.getMessage());
        }
    }

    private void setupBubbleTouch() {
        ImageView bubbleImg = floatingView.findViewById(R.id.bubble_img);
        if (bubbleImg == null) return;
        bubbleImg.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private long lastDownTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastDownTime = System.currentTimeMillis();
                        initialX = params.x; initialY = params.y;
                        initialTouchX = event.getRawX(); initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (System.currentTimeMillis() - lastDownTime < 200) {
                            toggleChatWindow();
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (initialTouchX - event.getRawX());
                        params.y = initialY + (int) (initialTouchY - event.getRawY());
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    private void toggleChatWindow() {
        if (isChatWindowVisible) {
            if (chatWindowView != null && chatWindowView.getWindowToken() != null) {
                windowManager.removeView(chatWindowView);
            }
            isChatWindowVisible = false;
        } else {
            showChatWindow();
        }
    }

    private void showChatWindow() {
        if (chatWindowView == null) {
            chatWindowView = LayoutInflater.from(themedContext).inflate(R.layout.chat_window, null);
            setupChatLogic();
        }

        int layoutType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams chatParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        chatParams.gravity = Gravity.CENTER;
        chatParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;

        windowManager.addView(chatWindowView, chatParams);
        isChatWindowVisible = true;
    }

    private void setupChatLogic() {
        recyclerView = chatWindowView.findViewById(R.id.recyclerViewChat);
        if (recyclerView != null) {
            adapter = new ChatAdapter(messageList);
            recyclerView.setLayoutManager(new LinearLayoutManager(themedContext));
            recyclerView.setAdapter(adapter);
        }

        chatWindowView.findViewById(R.id.btn_close).setOnClickListener(v -> toggleChatWindow());

        btnSend = chatWindowView.findViewById(R.id.btn_send);
        EditText etMessage = chatWindowView.findViewById(R.id.et_message);

        if (btnSend != null && etMessage != null) {
            btnSend.setOnClickListener(v -> {
                String msg = etMessage.getText().toString().trim();
                if (!msg.isEmpty()) {
                    addMessage(msg, false);
                    askAI(msg);
                    etMessage.setText("");
                }
            });
        }

        if (messageList.isEmpty()) {
            checkServerAndInit();
        }
    }

    // Kiểm tra server trước khi mở chat — chạy nền, không block UI
    private void checkServerAndInit() {
        if (btnSend != null) btnSend.setEnabled(false);

        new Thread(() -> {
            boolean reachable = false;
            try {
                URL url = new URL(BuildConfig.AI_SERVER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("GET");
                conn.connect();
                reachable = conn.getResponseCode() > 0;
                conn.disconnect();
            } catch (Exception ignored) {}

            final boolean serverOk = reachable;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (serverOk) {
                    addMessage("TechMate xin chào, quý khách có gì thắc mắc ạ?", true);
                    if (btnSend != null) btnSend.setEnabled(true);
                } else {
                    addMessage("Server AI hiện chưa hoạt động. Vui lòng thử lại sau.", true);
                    // btnSend giữ nguyên trạng thái disabled để tránh user gửi tin khi server tắt
                }
            });
        }).start();
    }

    private void addMessage(String text, boolean isAi) {
        messageList.add(new ChatMessage(text, isAi));
        if (adapter != null) {
            adapter.notifyItemInserted(messageList.size() - 1);
            if (recyclerView != null) {
                recyclerView.scrollToPosition(messageList.size() - 1);
            }
        }
    }

    private void askAI(String message) {
        AiApiService api = AiApiClient.getApiService();
        if (api == null) {
            addMessage("Lỗi cấu hình hệ thống AI.", true);
            return;
        }

        api.getAiResponse(new AiChatRequest(message)).enqueue(new Callback<AiChatResponse>() {
            @Override
            public void onResponse(Call<AiChatResponse> call, Response<AiChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    addMessage(response.body().getAnswer(), true);
                } else {
                    String errorDetail = "Lỗi Server (" + response.code() + ")";
                    if (response.code() == 404) errorDetail += ": Không tìm thấy API.";
                    else if (response.code() == 500) errorDetail += ": Server AI đang gặp sự cố.";
                    addMessage(errorDetail, true);
                }
            }

            @Override
            public void onFailure(Call<AiChatResponse> call, Throwable t) {
                if (t instanceof java.net.ConnectException || t instanceof java.net.SocketTimeoutException) {
                    addMessage("Server AI hiện chưa hoạt động. Vui lòng thử lại sau.", true);
                } else {
                    addMessage("Lỗi kết nối. Vui lòng kiểm tra mạng và thử lại.", true);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null && floatingView.getWindowToken() != null) windowManager.removeView(floatingView);
        if (isChatWindowVisible && chatWindowView != null && chatWindowView.getWindowToken() != null) windowManager.removeView(chatWindowView);
    }
}
