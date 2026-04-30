package com.example.test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class AnthropicClient {

    private final RestClient client;
    private final String apiKey;

    public AnthropicClient(@Value("${anthropic.api.key}") String apiKey) {
        this.client = RestClient.create("https://api.anthropic.com");
        this.apiKey = apiKey;
    }

    public String complete(String userMessage) {
        try {
            MessageRequest request = new MessageRequest(
                    "claude-sonnet-4-6",
                    1024,
                    List.of(new Message("user", userMessage))
            );
            MessageResponse response = client.post()
                    .uri("/v1/messages")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("content-type", "application/json")
                    .body(request)
                    .retrieve()
                    .body(MessageResponse.class);
            if (response == null || response.content() == null || response.content().isEmpty()) {
                return "No response from Claude.";
            }
            return response.content().get(0).text();
        } catch (Exception e) {
            return "Error contacting Claude: " + e.getMessage();
        }
    }

    record MessageRequest(String model, @JsonProperty("max_tokens") int maxTokens, List<Message> messages) {}

    record Message(String role, String content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MessageResponse(List<ContentBlock> content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ContentBlock(String type, String text) {}
}