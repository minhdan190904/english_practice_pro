package com.minhdan.english_practice_pro.speaking;

import com.minhdan.english_practice_pro.speaking.dto.WhisperTranscribeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

@Component
public class WhisperClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public WhisperClient(@Value("${whisper.baseUrl}") String baseUrl, ObjectMapper objectMapper) {
        this.baseUrl = baseUrl;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    public WhisperTranscribeResponse transcribe(byte[] audioBytes, String filename, String language) {
        var lang = (language == null || language.isBlank()) ? "en" : language;
        var name = (filename == null || filename.isBlank()) ? "audio.mp3" : filename;

        var file = new ByteArrayResource(audioBytes) {
            @Override
            public String getFilename() {
                return name;
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("language", lang);
        body.add("audio", file);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.postForEntity(baseUrl + "/transcribe", request, String.class);

        try {
            return objectMapper.readValue(resp.getBody(), WhisperTranscribeResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse whisper response: " + resp.getBody(), e);
        }
    }
}