package ch17.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

/**
 * A tiny, deterministic, offline embedding model: "feature hashing" of words
 * into a 256-dimensional vector, normalised to length 1. Texts that share words
 * get similar vectors - crude, but enough to make similarity search work.
 * Real embedding models (text-embedding-3-small, etc.) capture meaning, not just words.
 */
public class FakeEmbeddingModel implements EmbeddingModel {

    private static final int DIMENSIONS = 256;
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "is", "are", "to", "of", "in", "on", "for", "and", "or",
            "i", "my", "can", "do", "does", "how", "what", "when", "you", "your", "it", "be", "with");

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> out = new ArrayList<>();
        List<String> texts = request.getInstructions();
        for (int i = 0; i < texts.size(); i++) {
            out.add(new Embedding(vector(texts.get(i)), i));
        }
        return new EmbeddingResponse(out);
    }

    @Override
    public float[] embed(Document document) {
        return vector(document.getText());
    }

    @Override
    public int dimensions() { return DIMENSIONS; }

    static float[] vector(String text) {
        float[] v = new float[DIMENSIONS];
        for (String raw : text.toLowerCase().split("[^a-z0-9]+")) {
            if (raw.isBlank() || STOP_WORDS.contains(raw)) continue;
            String word = raw.length() > 4 ? raw.substring(0, 5) : raw; // crude stemming: refunds ~ refund
            v[Math.floorMod(word.hashCode(), DIMENSIONS)] += 1f;
        }
        double norm = 0;
        for (float x : v) norm += x * x;
        norm = Math.sqrt(norm);
        if (norm > 0) for (int i = 0; i < v.length; i++) v[i] /= (float) norm;
        return v;
    }
}
