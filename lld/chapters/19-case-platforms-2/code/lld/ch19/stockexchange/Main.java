package lld.ch19.stockexchange;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Usage: java lld.ch19.stockexchange.Main [orders.txt]  (no file = the sample from the problem statement). */
public class Main {
    static final List<String> SAMPLE = List.of(
            "#1 09:45 BAC sell 240.12 100",
            "#2 09:46 BAC sell 237.45 90",
            "#3 09:47 BAC buy 238.10 110",
            "#4 09:48 BAC buy 237.80 10",
            "#5 09:49 BAC buy 237.80 40",
            "#6 09:50 BAC sell 236.00 50");

    public static void main(String[] args) throws IOException {
        List<String> lines = args.length > 0 ? Files.readAllLines(Path.of(args[0])) : SAMPLE;

        System.out.println("== Trades (execution price = sell order's price) ==");
        MatchingEngine engine = new MatchingEngine(ExecutionPricePolicy.SELL_PRICE, System.out::println);
        lines.stream().filter(l -> !l.isBlank()).forEach(engine::submitLine);
        System.out.println("book after: " + engine.book("BAC").snapshot());

        System.out.println("\n== Same input, real-exchange rule (price of the resting order) ==");
        MatchingEngine real = new MatchingEngine(ExecutionPricePolicy.RESTING_PRICE, System.out::println);
        lines.stream().filter(l -> !l.isBlank()).forEach(real::submitLine);

        System.out.println("\n== Tests ==");
        int failed = MatchingEngineTests.runAll();
        if (failed > 0) System.exit(1);
    }
}
