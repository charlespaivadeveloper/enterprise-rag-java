package dev.charlespaiva.rag.usecase.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RrfFuser {

    private static final int K = 60;

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public List<RetrievedChunk> fuse(List<RetrievedChunk> semanticRanked, List<RetrievedChunk> lexicalRanked) {
        Map<UUID, double[]> scores = new HashMap<>();
        Map<UUID, RetrievedChunk> byId = new HashMap<>();

        for (int i = 0; i < semanticRanked.size(); i++) {
            RetrievedChunk rc = semanticRanked.get(i);
            UUID id = rc.chunk().id();
            scores.computeIfAbsent(id, ignored -> new double[2])[0] = 1.0 / (K + i + 1);
            byId.put(id, rc);
        }
        for (int i = 0; i < lexicalRanked.size(); i++) {
            RetrievedChunk rc = lexicalRanked.get(i);
            UUID id = rc.chunk().id();
            scores.computeIfAbsent(id, ignored -> new double[2])[1] = 1.0 / (K + i + 1);
            byId.putIfAbsent(id, rc);
        }

        List<RetrievedChunk> result = new ArrayList<>();
        for (Map.Entry<UUID, double[]> entry : scores.entrySet()) {
            UUID id = entry.getKey();
            double[] s = entry.getValue();
            double rrfScore = s[0] + s[1];
            RetrievedChunk original = byId.get(id);
            result.add(new RetrievedChunk(original.chunk(), original.semanticScore(), original.lexicalScore(), rrfScore));
        }
        result.sort(Comparator.comparingDouble(RetrievedChunk::finalScore).reversed());
        return result;
    }
}
