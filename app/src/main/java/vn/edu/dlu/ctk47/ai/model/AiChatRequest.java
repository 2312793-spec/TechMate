package vn.edu.dlu.ctk47.AI.api;

public class AiChatRequest {
    private String prompt;

    public AiChatRequest(String prompt) {
        this.prompt = prompt;
    }

    // Getter và Setter
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
}