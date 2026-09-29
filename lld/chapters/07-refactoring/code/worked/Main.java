package lld.ch07.worked;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Characterisation check - the safety net for the whole worked example.
 * Every step must print exactly what the messy STEP 0 printed. If one byte differs,
 * that step was not a refactoring (it changed behaviour) and we exit with an error.
 */
public class Main {

    public static void main(String[] args) {
        Map<String, Supplier<String>> steps = new LinkedHashMap<>();
        steps.put("step0 (messy original)", lld.ch07.worked.step0.Main::run);
        steps.put("step1 extract + rename", lld.ch07.worked.step1.Main::run);
        steps.put("step2 split loop + pipeline", lld.ch07.worked.step2.Main::run);
        steps.put("step3 split phase", lld.ch07.worked.step3.Main::run);
        steps.put("step4 primitive -> object", lld.ch07.worked.step4.Main::run);
        steps.put("step5 polymorphism (final)", lld.ch07.worked.step5.Main::run);

        String golden = lld.ch07.worked.step0.Main.run();
        System.out.print("Golden output from step 0:\n" + golden);
        System.out.println("---");
        boolean allSame = true;
        for (Map.Entry<String, Supplier<String>> e : steps.entrySet()) {
            boolean same = golden.equals(e.getValue().get());
            allSame &= same;
            System.out.printf("%-30s %s%n", e.getKey(), same ? "SAME output" : "DIFFERENT output!");
        }
        if (!allSame) {
            System.err.println("A step changed behaviour - that was not a refactoring.");
            System.exit(1);
        }
        System.out.println("All steps preserve behaviour.");
    }
}
