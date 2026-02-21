package com.minhdan.english_practice_pro.speaking;

import com.minhdan.english_practice_pro.speaking.dto.SpeakingAssessmentResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/speaking")
public class SpeakingController {
    private final WhisperClient whisperClient;
    private final ScoringService scoringService;

    public SpeakingController(WhisperClient whisperClient, ScoringService scoringService) {
        this.whisperClient = whisperClient;
        this.scoringService = scoringService;
    }

    @PostMapping(value = "/assess", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SpeakingAssessmentResponse assess(
            @RequestPart("audio") MultipartFile audio,
            @RequestPart("mode") String mode,
            @RequestPart(value = "referenceText", required = false) String referenceText,
            @RequestPart(value = "language", required = false) String language
    ) throws Exception {

        var whisper = whisperClient.transcribe(audio.getBytes(), audio.getOriginalFilename(), language);

        if ("read_aloud".equalsIgnoreCase(mode)) {
            if (referenceText == null || referenceText.isBlank()) {
                throw new IllegalArgumentException("referenceText is required for read_aloud");
            }
            return scoringService.assessReadAloud(whisper.text(), whisper.duration(), whisper.segments(), referenceText);
        }

        return scoringService.assessFree(whisper.text(), whisper.duration(), whisper.segments());
    }
}