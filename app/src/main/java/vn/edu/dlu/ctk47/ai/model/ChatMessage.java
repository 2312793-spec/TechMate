package vn.edu.dlu.ctk47.ai.model;

public class ChatMessage {
    private String text;
    private boolean isAi;

    public ChatMessage(String text, boolean isAi) {
        this.text = text;
        this.isAi = isAi;
    }

    public String getText() { return text; }
    public boolean isAi() { return isAi; }
}