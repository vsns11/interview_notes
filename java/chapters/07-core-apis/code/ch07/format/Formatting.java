package ch07.format;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.HexFormat;
import java.util.Locale;

public class Formatting {
    public static void main(String[] args) {
        String name = "Ada";
        int qty = 1234567;
        double price = 3.14159;

        System.out.println(String.format("|%s|%10s|%-10s|%.2s|", name, name, name, name));
        System.out.println(String.format("|%d|%8d|%-8d|%08d|%,d|%+d|", 42, 42, 42, 42, qty, 42));
        System.out.println(String.format("|%f|%.2f|%10.3f|%-10.1f|%e|", price, price, price, price, 12345.678));
        System.out.println(String.format("|%x|%X|%o|%c|%b|%%|", 255, 255, 8, 'J', null));
        System.out.println(String.format("|%2$s %1$s|%s|", "world", "hello"));
        System.out.println("%s bought %d items for $%.2f".formatted(name, 3, 9.5)); // Java 15
        System.out.printf("printf with %%n line ending%n");

        // Locale-sensitive formatting: be explicit in servers and tests
        System.out.println(String.format(Locale.US, "US: %,.2f", 1234567.891));
        System.out.println(String.format(Locale.GERMANY, "DE: %,.2f", 1234567.891));

        NumberFormat usd = NumberFormat.getCurrencyInstance(Locale.US);
        NumberFormat pct = NumberFormat.getPercentInstance(Locale.US);
        NumberFormat compact = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT); // Java 12
        System.out.println("currency=" + usd.format(new BigDecimal("1234.5")) + " percent=" + pct.format(0.256)
                + " compact=" + compact.format(1_250_000));

        DecimalFormat df = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        System.out.println("DecimalFormat: " + df.format(1234.5) + " " + df.format(0.5) + " " + df.format(-7));

        HexFormat hex = HexFormat.ofDelimiter(":").withUpperCase();                  // Java 17
        System.out.println("HexFormat: " + hex.formatHex(new byte[]{10, -1, 127}));
    }
}
