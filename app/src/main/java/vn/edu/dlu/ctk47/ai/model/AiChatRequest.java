package vn.edu.dlu.ctk47.ai.model;

import com.google.gson.annotations.SerializedName;

public class AiChatRequest {

    // BẮT BUỘC: Phải có chữ "message" viết thường ở đây để Python hiểu được
    @SerializedName("message")
    private String message;

    public AiChatRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}