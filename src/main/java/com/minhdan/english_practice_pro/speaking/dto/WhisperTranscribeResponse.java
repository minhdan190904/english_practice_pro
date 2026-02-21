package com.minhdan.english_practice_pro.speaking.dto;

import java.util.List;

public record WhisperTranscribeResponse(
        String text,
        String language,
        double duration,
        List<WhisperSegment> segments
) {}