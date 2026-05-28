package vn.edu.dlu.ctk47.ai.model;

import com.google.gson.annotations.SerializedName;

public class AiChatResponse {

    @SerializedName("response")
    private String response;

    @SerializedName("reply")
    private String reply;

    public String getAnswer() {
        // Ưu tiên lấy trường response khớp với Python đã sửa
        if (response != null) return response;
        if (reply != null) return reply;
        return "Không nhận được nội dung từ Server AI";
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}