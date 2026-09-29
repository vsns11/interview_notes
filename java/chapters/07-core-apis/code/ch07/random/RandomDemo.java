package ch07.random;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class RandomDemo {
    public static void main(String[] args) {
        // Seeded Random: the same seed always gives the same sequence (great for tests and simulations)
        Random r1 = new Random(42), r2 = new Random(42);
        System.out.print("seed 42 twice: ");
        for (int i = 0; i < 5; i++) System.out.print(r1.nextInt(100) + "/" + r2.nextInt(100) + " ");
        System.out.println();

        Random r = new Random(7);
        System.out.println("nextInt(1,7) dice: " + r.nextInt(1, 7) + " " + r.nextInt(1, 7) + " " + r.nextInt(1, 7)  // (origin, bound): Java 17
                + " | nextBoolean=" + r.nextBoolean() + " | nextDouble in [0,1)? " + (r.nextDouble() < 1.0));
        System.out.printf("nextGaussian (mean 0, sd 1): %.4f%n", r.nextGaussian());

        List<String> deck = new ArrayList<>(List.of("A", "B", "C", "D", "E"));
        Collections.shuffle(deck, new Random(1));
        System.out.println("shuffle with seed 1: " + deck);
        System.out.println("ints stream: " + new Random(3).ints(5, 0, 10).boxed().toList());

        // Newer algorithms via RandomGenerator (Java 17, JEP 356)
        RandomGenerator lxm = RandomGeneratorFactory.of("L64X128MixRandom").create(42L);
        System.out.println("L64X128MixRandom seeded: " + lxm.nextInt(100) + " " + lxm.nextInt(100));
        RandomGenerator def = RandomGenerator.getDefault();
        System.out.println("default algorithm: " + def.getClass().getSimpleName());

        // Concurrent code: ThreadLocalRandom (no contention). Security: SecureRandom.
        int roll = ThreadLocalRandom.current().nextInt(1, 7);
        System.out.println("ThreadLocalRandom roll in 1..6? " + (roll >= 1 && roll <= 6));
        byte[] token = new byte[16];
        new SecureRandom().nextBytes(token);
        System.out.println("SecureRandom token bytes: " + token.length);
    }
}
