package com.minhdan.english_practice_pro.speaking.scoring;

import com.minhdan.english_practice_pro.speaking.dto.DiffToken;
import java.util.*;

public class WerDiff {
    public record WerResult(int S, int D, int I, int N, List<DiffToken> diff) {}

    public static WerResult compute(String reference, String hypothesis) {
        var ref = tokenize(reference);
        var hyp = tokenize(hypothesis);

        int n = ref.size();
        int m = hyp.size();

        int[][] dp = new int[n + 1][m + 1];
        int[][] op = new int[n + 1][m + 1];

        for (int i = 0; i <= n; i++) {
            dp[i][0] = i;
            op[i][0] = 2;
        }
        for (int j = 0; j <= m; j++) {
            dp[0][j] = j;
            op[0][j] = 3;
        }
        op[0][0] = 0;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int cost = ref.get(i - 1).equals(hyp.get(j - 1)) ? 0 : 1;
                int sub = dp[i - 1][j - 1] + cost;
                int del = dp[i - 1][j] + 1;
                int ins = dp[i][j - 1] + 1;

                int best = sub;
                int bop = cost == 0 ? 1 : 4;

                if (del < best) { best = del; bop = 2; }
                if (ins < best) { best = ins; bop = 3; }

                dp[i][j] = best;
                op[i][j] = bop;
            }
        }

        int i = n, j = m;
        var diff = new ArrayList<DiffToken>();
        int S = 0, D = 0, I = 0;

        while (i > 0 || j > 0) {
            int code = op[i][j];
            if (code == 1) {
                diff.add(new DiffToken("ok", ref.get(i - 1), i - 1, j - 1));
                i--; j--;
            } else if (code == 4) {
                diff.add(new DiffToken("sub", ref.get(i - 1) + "→" + hyp.get(j - 1), i - 1, j - 1));
                S++; i--; j--;
            } else if (code == 2) {
                diff.add(new DiffToken("del", ref.get(i - 1), i - 1, j));
                D++; i--;
            } else {
                diff.add(new DiffToken("ins", hyp.get(j - 1), i, j - 1));
                I++; j--;
            }
        }

        Collections.reverse(diff);
        return new WerResult(S, D, I, n, diff);
    }

    private static List<String> tokenize(String s) {
        if (s == null) return List.of();
        var cleaned = s.toLowerCase()
                .replaceAll("[^a-z0-9'\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.isBlank()) return List.of();
        return Arrays.asList(cleaned.split(" "));
    }
}