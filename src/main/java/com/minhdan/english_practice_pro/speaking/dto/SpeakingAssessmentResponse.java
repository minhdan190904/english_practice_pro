package com.minhdan.english_practice_pro.speaking.dto;

import java.util.List;

public record SpeakingAssessmentResponse(
        String mode,
        String transcript,
        double duration,
        Double accuracyScore,
        Double completenessScore,
        Double fluencyScore,
        Double overallScore,
        List<DiffToken> diff
) {}