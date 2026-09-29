package lld.ch21.ledger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * LOAN BANK BORROWER PRINCIPAL YEARS RATE
 * PAYMENT BANK BORROWER LUMP_SUM EMI_NO
 * BALANCE BANK BORROWER EMI_NO      -> prints "BANK BORROWER AMOUNT_PAID EMIS_LEFT"
 * Only BALANCE produces output (as in the original statement); errors are printed with "ERROR".
 */
public class CommandProcessor {
    private final LedgerService svc;
    public CommandProcessor(LedgerService svc) { this.svc = svc; }

    public List<String> run(List<String> lines) {
        List<String> output = new ArrayList<>();
        for (String line : lines) {
            if (line.isBlank()) continue;
            String[] t = line.trim().split("\\s+");
            try {
                switch (t[0]) {
                    case "LOAN" -> { need(t, 6); svc.loan(t[1], t[2], num(t[3]), Integer.parseInt(t[4]), num(t[5])); }
                    case "PAYMENT" -> { need(t, 5); svc.payment(t[1], t[2], num(t[3]), Integer.parseInt(t[4])); }
                    case "BALANCE" -> { need(t, 4); output.add(svc.balance(t[1], t[2], Integer.parseInt(t[3])).toString()); }
                    default -> throw new LedgerException("Unknown command " + t[0]);
                }
            } catch (LedgerException | NumberFormatException e) {
                output.add("ERROR " + e.getMessage() + "  [" + line + "]");
            }
        }
        return output;
    }

    private static BigDecimal num(String s) {
        try { return new BigDecimal(s); } catch (NumberFormatException e) { throw new LedgerException("Not a number: " + s); }
    }

    private static void need(String[] t, int n) {
        if (t.length != n) throw new LedgerException(t[0] + " expects " + (n - 1) + " arguments");
    }
}
