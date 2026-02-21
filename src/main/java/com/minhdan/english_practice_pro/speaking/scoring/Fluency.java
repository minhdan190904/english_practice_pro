package com.minhdan.english_practice_pro.speaking.scoring;

import com.minhdan.english_practice_pro.speaking.dto.WhisperSegment;
import java.util.List;

public class Fluency {
    public record FluencyResult(double wpm, double pauseRatio, double score) {}

    public static FluencyResult score(String transcript, double durationSeconds, List<WhisperSegment> segments) {
        int words = countWords(transcript);
        double wpm = durationSeconds > 0 ? (words / (durationSeconds / 60.0)) : 0.0;

        double pauses = 0.0;
        if (segments != null && segments.size() >= 2) {
            for (int i = 1; i < segments.size(); i++) {
                double gap = segments.get(i).start() - segments.get(i - 1).end();
                if (gap > 0.35) pauses += gap;
            }
        }

        double pauseRatio = durationSeconds > 0 ? (pauses / durationSeconds) : 0.0;

        double base = 100.0;
        base -= Math.abs(wpm - 140.0);
        base -= pauseRatio * 120.0;

        return new FluencyResult(wpm, pauseRatio, clamp(base));
    }

    private static int countWords(String s) {
        if (s == null) return 0;
        var cleaned = s.toLowerCase()
                .replaceAll("[^a-z0-9'\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.isBlank()) return 0;
        return cleaned.split(" ").length;
    }

    private static double clamp(double v) {
        if (v < 0) return 0;
        if (v > 100) return 100;
        return v;
    }
}