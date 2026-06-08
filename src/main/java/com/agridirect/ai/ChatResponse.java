package com.agridirect.ai;

import java.util.List;

public class ChatResponse {
    private String reply;
    private String language;
    private List<String> suggestions;
    private List<String> relatedTopics;

    public ChatResponse() {}

    public ChatResponse(String reply, String language) {
        this.reply = reply;
        this.language = language;
    }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

    public List<String> getRelatedTopics() { return relatedTopics; }
    public void setRelatedTopics(List<String> relatedTopics) { this.relatedTopics = relatedTopics; }
}
