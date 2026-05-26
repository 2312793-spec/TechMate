package vn.edu.dlu.ctk47.ai.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import vn.edu.dlu.ctk47.ai.model.AiChatRequest;
import vn.edu.dlu.ctk47.ai.model.AiChatResponse;

public interface AiApiService {

    @POST("chat")
    Call<AiChatResponse> getAiResponse(@Body AiChatRequest request);
}