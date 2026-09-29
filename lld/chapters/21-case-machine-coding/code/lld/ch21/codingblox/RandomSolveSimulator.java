package lld.ch21.codingblox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class RandomSolveSimulator implements SolveSimulator {
    private final Random random;
    public RandomSolveSimulator(long seed) { this.random = new Random(seed); }

    @Override public List<Question> solvedBy(String user, List<Question> pool) {
        if (pool.isEmpty()) return List.of();
        List<Question> copy = new ArrayList<>(pool);
        Collections.shuffle(copy, random);
        int k = 1 + random.nextInt(copy.size());          // solves between 1 and all questions
        List<Question> picked = new ArrayList<>(copy.subList(0, k));
        picked.sort(Comparator.comparingInt(Question::id));
        return picked;
    }
}
