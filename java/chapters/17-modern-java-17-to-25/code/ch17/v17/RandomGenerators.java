package ch17.v17;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/** JEP 356 (Java 17): one interface for all random generators, plus new algorithms. */
public class RandomGenerators {
    public static void main(String[] args) {
        RandomGenerator g = RandomGeneratorFactory.of("L64X128MixRandom").create(42); // seeded: repeatable
        System.out.println("L64X128MixRandom dice: " + g.ints(8, 1, 7).boxed().toList());

        RandomGenerator legacy = new java.util.Random(42);           // old classes implement the interface too
        System.out.println("java.util.Random dice: " + legacy.ints(8, 1, 7).boxed().toList());

        System.out.println("default algorithm    : " + RandomGenerator.getDefault().getClass().getSimpleName());
        System.out.println("a few algorithms     : " + RandomGeneratorFactory.all()
                .map(RandomGeneratorFactory::name).sorted().limit(6).toList());
    }
}
