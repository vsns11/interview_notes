package lld.ch21.ledger;

import java.util.List;

public class Main {
    static final List<String> SAMPLE_1 = List.of(
            "LOAN IDIDI Dale 10000 5 4",
            "LOAN MBI Harry 2000 2 2",
            "BALANCE IDIDI Dale 5",
            "BALANCE IDIDI Dale 40",
            "BALANCE MBI Harry 12",
            "BALANCE MBI Harry 0");

    static final List<String> SAMPLE_2 = List.of(
            "LOAN IDIDI Dale 5000 1 6",
            "LOAN MBI Harry 10000 3 7",
            "LOAN UON Shelly 15000 2 9",
            "PAYMENT IDIDI Dale 1000 5",
            "PAYMENT MBI Harry 5000 10",
            "PAYMENT UON Shelly 7000 12",
            "BALANCE IDIDI Dale 3",
            "BALANCE IDIDI Dale 6",
            "BALANCE UON Shelly 12",
            "BALANCE MBI Harry 12");

    static final List<String> EDGE_CASES = List.of(
            "LOAN SBI Asha 1000 1 12",
            "PAYMENT SBI Asha 2000 1",            // lump sum bigger than the rest of the loan
            "BALANCE SBI Asha 2",
            "LOAN SBI Asha 5 1 1",                // duplicate loan
            "BALANCE HDFC Nobody 1",              // unknown loan
            "BALANCE SBI Asha 13",                // beyond tenure
            "PAYMENT SBI Asha ten 1",             // bad number
            "REFUND SBI Asha 1");                 // unknown command

    public static void main(String[] args) {
        run("Sample 1", SAMPLE_1);
        run("Sample 2", SAMPLE_2);
        run("Edge cases", EDGE_CASES);
    }

    private static void run(String title, List<String> input) {
        System.out.println("== " + title);
        new CommandProcessor(new LedgerService(new SimpleInterest())).run(input).forEach(System.out::println);
    }
}
