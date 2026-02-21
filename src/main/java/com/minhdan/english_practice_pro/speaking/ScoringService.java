package com.minhdan.english_practice_pro.speaking;

import com.minhdan.english_practice_pro.speaking.dto.SpeakingAssessmentResponse;
import com.minhdan.english_practice_pro.speaking.dto.WhisperSegment;
import com.minhdan.english_practice_pro.speaking.scoring.Fluency;
import com.minhdan.english_practice_pro.speaking.scoring.WerDiff;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ScoringService {

    public SpeakingAssessmentResponse assessReadAloud(String transcript, double duration, List<WhisperSegment> segments, String referenceText) {
        var wer = WerDiff.compute(referenceText, transcript);
        double WER = wer.N() == 0 ? 1.0 : (wer.S() + wer.D() + wer.I()) / (double) wer.N();
        double accuracy = clamp(100.0 * (1.0 - WER));
        double completeness = wer.N() == 0 ? 0.0 : clamp(100.0 * ((wer.N() - wer.D()) / (double) wer.N()));
        var flu = Fluency.score(transcript, duration, segments);
        double overall = clamp(0.6 * accuracy + 0.2 * completeness + 0.2 * flu.score());

        return new SpeakingAssessmentResponse(
                "read_aloud",
                transcript,
                duration,
                accuracy,
                completeness,
                flu.score(),
                overall,
                wer.diff()
        );
    }

    public SpeakingAssessmentResponse assessFree(String transcript, double duration, List<WhisperSegment> segments) {
        var flu = Fluency.score(transcript, duration, segments);
        return new SpeakingAssessmentResponse(
                "free",
                transcript,
                duration,
                null,
                null,
                flu.score(),
                flu.score(),
                List.of()
        );
    }

    private static double clamp(double v) {
        if (v < 0) return 0;
        if (v > 100) return 100;
        return v;
    }
}