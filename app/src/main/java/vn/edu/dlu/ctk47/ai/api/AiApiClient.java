package vn.edu.dlu.ctk47.ai.api;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;
import vn.edu.dlu.ctk47.techmate.BuildConfig;

public class AiApiClient {

    // URL được lấy từ BuildConfig — chỉ cần sửa trong build.gradle.kts, không cần sửa file này
    private static final String BASE_URL = BuildConfig.AI_SERVER_URL;
    private static Retrofit retrofit = null;

    public static AiApiService getApiService() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                                .header("ngrok-skip-browser-warning", "true")
                                .header("Content-Type", "application/json")
                                .build();
                        Log.d("AiApiClient", "--> GỬI ĐẾN: " + request.url());
                        Response response = chain.proceed(request);
                        Log.d("AiApiClient", "<-- MÃ PHẢN HỒI: " + response.code());
                        return response;
                    })
                    .build();

            try {
                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            } catch (Exception e) {
                Log.e("AiApiClient", "Lỗi cấu hình Retrofit: " + e.getMessage());
            }
        }
        return (retrofit != null) ? retrofit.create(AiApiService.class) : null;
    }
}