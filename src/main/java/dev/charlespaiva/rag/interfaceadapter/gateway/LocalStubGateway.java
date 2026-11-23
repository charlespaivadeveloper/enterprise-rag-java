package dev.charlespaiva.rag.interfaceadapter.gateway;

import dev.charlespaiva.rag.usecase.common.RetrievedChunk;
import dev.charlespaiva.rag.usecase.dataaccess.EmbeddingServiceInterface;
import dev.charlespaiva.rag.usecase.dataaccess.GenerationServiceInterface;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Component
@Profile("local-stub")
public class LocalStubGateway implements EmbeddingServiceInterface, GenerationServiceInterface {

    @Override
    public List<float[]> embed(List<String> texts) {
        return texts.stream().map(this::embed).toList();
    }

    @Override
    public float[] embed(String text) {
        float[] v = new float[1536];
        byte[] b = text.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < b.length; i++) {
            v[i % v.length] += (b[i] & 255) / 255f;
        }
        return v;
    }

    @Override
    public String generate(String q, List<RetrievedChunk> e) {
        return "Resposta demonstrativa baseada em " + e.size() + " evidência(s): " + e.getFirst().chunk().content();
    }
}
